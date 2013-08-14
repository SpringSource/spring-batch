/*
 * Copyright 2006-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.core.scope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.util.StringValueResolver;

/**
 * Scope for step context. Objects in this scope use the Spring container as an
 * object factory, so there is only one instance of such a bean per executing
 * step. All objects in this scope are &lt;aop:scoped-proxy/&gt; (no need to
 * decorate the bean definitions).<br/>
 * <br/>
 *
 * In addition, support is provided for late binding of references accessible
 * from the {@link StepContext} using #{..} placeholders. Using this feature,
 * bean properties can be pulled from the step or job execution context and the
 * job parameters. E.g.
 *
 * <pre>
 * &lt;bean id=&quot;...&quot; class=&quot;...&quot; scope=&quot;step&quot;&gt;
 * 	&lt;property name=&quot;parent&quot; ref=&quot;#{stepExecutionContext[helper]}&quot; /&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id=&quot;...&quot; class=&quot;...&quot; scope=&quot;step&quot;&gt;
 * 	&lt;property name=&quot;name&quot; value=&quot;#{stepExecutionContext['input.name']}&quot; /&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id=&quot;...&quot; class=&quot;...&quot; scope=&quot;step&quot;&gt;
 * 	&lt;property name=&quot;name&quot; value=&quot;#{jobParameters[input]}&quot; /&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id=&quot;...&quot; class=&quot;...&quot; scope=&quot;step&quot;&gt;
 * 	&lt;property name=&quot;name&quot; value=&quot;#{jobExecutionContext['input.stem']}.txt&quot; /&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * The {@link StepContext} is referenced using standard bean property paths (as
 * per {@link BeanWrapper}). The examples above all show the use of the Map
 * accessors provided as a convenience for step and job attributes.
 *
 * @author Dave Syer
 * @author Michael Minella
 * @since 2.0
 */
public class StepScope implements Scope, BeanFactoryPostProcessor, Ordered {
	
	private static final String TARGET_NAME_PREFIX = "scopedTarget.";

	private Log logger = LogFactory.getLog(getClass());

	private int order = Ordered.LOWEST_PRECEDENCE;

	private boolean autoProxy = true;

	private final Object mutex = new Object();

	/**
	 * @param order the order value to set priority of callback execution for
	 * the {@link BeanFactoryPostProcessor} part of this scope bean.
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return order;
	}

	/**
	 * Context key for clients to use for conversation identifier.
	 */
	public static final String ID_KEY = "STEP_IDENTIFIER";

	private String name = "step";

	private boolean proxyTargetClass = false;

	/**
	 * Flag to indicate that proxies should use dynamic subclassing. This allows
	 * classes with no interface to be proxied. Defaults to false.
	 *
	 * @param proxyTargetClass set to true to have proxies created using dynamic
	 * subclasses
	 */
	public void setProxyTargetClass(boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
	}

	/**
	 * Flag to indicate that bean definitions need not be auto proxied. This gives control back to the declarer of the
	 * bean definition (e.g. in an &#64;Configuration class).
	 *
	 * @param autoProxy the flag value to set (default true)
	 */
	public void setAutoProxy(boolean autoProxy) {
		this.autoProxy = autoProxy;
	}

	/**
	 * This will be used to resolve expressions in step-scoped beans.
	 */
	@Override
	public Object resolveContextualObject(String key) {
		StepContext context = getContext();
		// TODO: support for attributes as well maybe (setters not exposed yet
		// so not urgent).
		return new BeanWrapperImpl(context).getPropertyValue(key);
	}

	/**
	 * @see Scope#get(String, ObjectFactory)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object get(String name, ObjectFactory objectFactory) {
		StepContext context = getContext();
		Object scopedObject = context.getAttribute(name);

		if (scopedObject == null) {

			synchronized (mutex) {
				scopedObject = context.getAttribute(name);
				if (scopedObject == null) {

					logger.debug(String.format("Creating object in scope=%s, name=%s", this.name, name));

					scopedObject = objectFactory.getObject();
					context.setAttribute(name, scopedObject);

				}

			}

		}
		return scopedObject;
	}

	/**
	 * @see Scope#getConversationId()
	 */
	@Override
	public String getConversationId() {
		StepContext context = getContext();
		return context.getId();
	}

	/**
	 * @see Scope#registerDestructionCallback(String, Runnable)
	 */
	@Override
	public void registerDestructionCallback(String name, Runnable callback) {
		StepContext context = getContext();
		logger.debug(String.format("Registered destruction callback in scope=%s, name=%s", this.name, name));
		context.registerDestructionCallback(name, callback);
	}

	/**
	 * @see Scope#remove(String)
	 */
	@Override
	public Object remove(String name) {
		StepContext context = getContext();
		logger.debug(String.format("Removing from scope=%s, name=%s", this.name, name));
		return context.removeAttribute(name);
	}

	/**
	 * Get an attribute accessor in the form of a {@link StepContext} that can
	 * be used to store scoped bean instances.
	 *
	 * @return the current step context which we can use as a scope storage
	 * medium
	 */
	private StepContext getContext() {
		StepContext context = StepSynchronizationManager.getContext();
		if (context == null) {
			throw new IllegalStateException("No context holder available for step scope");
		}
		return context;
	}

	/**
	 * Register this scope with the enclosing BeanFactory.
	 *
	 * @see BeanFactoryPostProcessor#postProcessBeanFactory(ConfigurableListableBeanFactory)
	 *
	 * @param beanFactory the BeanFactory to register with
	 * @throws BeansException if there is a problem.
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

		beanFactory.registerScope(name, this);

		if(!autoProxy) {
			return;
		}

		Assert.state(beanFactory instanceof BeanDefinitionRegistry,
				"BeanFactory was not a BeanDefinitionRegistry, so StepScope cannot be used.");
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

		for (String beanName : beanFactory.getBeanDefinitionNames()) {
			if (!beanName.startsWith(TARGET_NAME_PREFIX)) {
				BeanDefinition definition = beanFactory.getBeanDefinition(beanName);			
				// Replace this or any of its inner beans with scoped proxy if it
				// has this scope
				boolean scoped = name.equals(definition.getScope());
				Scopifier scopifier = new Scopifier(registry, name, proxyTargetClass, scoped);
				scopifier.visitBeanDefinition(definition);

				if (scoped && !definition.isAbstract()) {
					createScopedProxy(beanName, definition, registry, proxyTargetClass);
				}
			}
		}

	}

	/**
	 * Public setter for the name property. This can then be used as a bean
	 * definition attribute, e.g. scope="step". Defaults to "step".
	 *
	 * @param name the name to set for this scope.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Wrap a target bean definition in a proxy that defers initialization until
	 * after the {@link StepContext} is available. Amounts to adding
	 * &lt;aop-auto-proxy/&gt; to a step scoped bean.
	 *
	 * @param beanName the bean name to replace
	 * @param definition the bean definition to replace
	 * @param registry the enclosing {@link BeanDefinitionRegistry}
	 * @param proxyTargetClass true if we need to force use of dynamic
	 * subclasses
	 * @return a {@link BeanDefinitionHolder} for the new representation of the
	 * target. Caller should register it if needed to be visible at top level in
	 * bean factory.
	 */
	private static BeanDefinitionHolder createScopedProxy(String beanName, BeanDefinition definition,
			BeanDefinitionRegistry registry, boolean proxyTargetClass) {

		BeanDefinitionHolder proxyHolder;

		proxyHolder = ScopedProxyUtils.createScopedProxy(new BeanDefinitionHolder(definition, beanName), registry,
				proxyTargetClass);

		registry.registerBeanDefinition(beanName, proxyHolder.getBeanDefinition());

		return proxyHolder;

	}

	/**
	 * Helper class to scan a bean definition hierarchy and force the use of
	 * auto-proxy for step scoped beans.
	 *
	 * @author Dave Syer
	 *
	 */
	private static class Scopifier extends BeanDefinitionVisitor {

		private final boolean proxyTargetClass;

		private final BeanDefinitionRegistry registry;

		private final String scope;

		private final boolean scoped;

		public Scopifier(BeanDefinitionRegistry registry, String scope, boolean proxyTargetClass, boolean scoped) {
			super(new StringValueResolver() {
				@Override
				public String resolveStringValue(String value) {
					return value;
				}
			});
			this.registry = registry;
			this.proxyTargetClass = proxyTargetClass;
			this.scope = scope;
			this.scoped = scoped;
		}

		@Override
		protected Object resolveValue(Object value) {

			BeanDefinition definition = null;
			String beanName = null;
			if (value instanceof BeanDefinition) {
				definition = (BeanDefinition) value;
				beanName = BeanDefinitionReaderUtils.generateBeanName(definition, registry);
			}
			else if (value instanceof BeanDefinitionHolder) {
				BeanDefinitionHolder holder = (BeanDefinitionHolder) value;
				definition = holder.getBeanDefinition();
				beanName = holder.getBeanName();
			}

			if (definition != null) {
				boolean nestedScoped = scope.equals(definition.getScope());
				boolean scopeChangeRequiresProxy = !scoped && nestedScoped;
				if (scopeChangeRequiresProxy) {
					// Exit here so that nested inner bean definitions are not
					// analysed
					return createScopedProxy(beanName, definition, registry, proxyTargetClass);
				}
			}

			// Nested inner bean definitions are recursively analysed here
			value = super.resolveValue(value);
			return value;

		}

	}
}
