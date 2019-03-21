/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.batch.test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Wrapper for a {@link DataSource} that can run scripts on start up and shut
 * down.  Us as a bean definition <br><br>
 * 
 * Run this class to initialize a database in a running server process.
 * Make sure the server is running first by launching the "hsql-server" from the
 * <code>hsql.server</code> project. Then you can right click in Eclipse and
 * Run As -&gt; Java Application. Do the same any time you want to wipe the
 * database and start again.
 * 
 * @author Dave Syer
 * 
 */
public class DataSourceInitializer implements InitializingBean, DisposableBean {

	private static final Log logger = LogFactory.getLog(DataSourceInitializer.class);

	private Resource[] initScripts;

	private Resource[] destroyScripts;

	private DataSource dataSource;

	private boolean ignoreFailedDrop = true;

	private boolean initialized = false;

	/**
	 * Main method as convenient entry point.
	 * 
	 * @param args arguments to be passed to main.
	 */
	@SuppressWarnings("resource")
	public static void main(String... args) {
		new ClassPathXmlApplicationContext(ClassUtils.addResourcePathToPackagePath(DataSourceInitializer.class,
				DataSourceInitializer.class.getSimpleName() + "-context.xml"));
	}

    @Override
	public void destroy() {
		if (destroyScripts==null) return;
		for (int i = 0; i < destroyScripts.length; i++) {
			Resource destroyScript = destroyScripts[i];
			try {
				doExecuteScript(destroyScript);
			}
			catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.warn("Could not execute destroy script [" + destroyScript + "]", e);
				}
				else {
					logger.warn("Could not execute destroy script [" + destroyScript + "]");
				}
			}
		}
	}

    @Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(dataSource, "A DataSource is required");
		initialize();
	}

	private void initialize() {
		if (!initialized) {
			destroy();
			if (initScripts != null) {
				for (int i = 0; i < initScripts.length; i++) {
					Resource initScript = initScripts[i];
					doExecuteScript(initScript);
				}
			}
			initialized = true;
		}
	}

	private void doExecuteScript(final Resource scriptResource) {
		if (scriptResource == null || !scriptResource.exists())
			return;
		TransactionTemplate transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
		transactionTemplate.execute(new TransactionCallback<Void>() {

            @Override
			public Void doInTransaction(TransactionStatus status) {
				JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
				String[] scripts;
				try {
					scripts = StringUtils
							.delimitedListToStringArray(stripComments(getScriptLines(scriptResource)), ";");
				}
				catch (IOException e) {
					throw new BeanInitializationException("Cannot load script from [" + scriptResource + "]", e);
				}
				for (int i = 0; i < scripts.length; i++) {
					String script = scripts[i].trim();
					if (StringUtils.hasText(script)) {
						try {
							jdbcTemplate.execute(script);
						}
						catch (DataAccessException e) {
							if (ignoreFailedDrop && script.toLowerCase().startsWith("drop")) {
								logger.debug("DROP script failed (ignoring): " + script);
							}
							else {
								throw e;
							}
						}
					}
				}
				return null;
			}

		});

	}

	private List<String> getScriptLines(Resource scriptResource) throws IOException {
		URI uri = scriptResource.getURI();
		initFileSystem(uri);
		return Files.readAllLines(Paths.get(uri), StandardCharsets.UTF_8);
	}

	private void initFileSystem(URI uri) throws IOException {
		try {
			FileSystems.getFileSystem(uri);
		}
		catch (FileSystemNotFoundException e) {
			FileSystems.newFileSystem(uri, Collections.emptyMap());
		}
		catch (IllegalArgumentException e) {
			FileSystems.getDefault();
		}
	}

	private String stripComments(List<String> list) {
		StringBuilder buffer = new StringBuilder();
		for (String line : list) {
			if (!line.startsWith("//") && !line.startsWith("--")) {
				buffer.append(line).append("\n");
			}
		}
		return buffer.toString();
	}

	public void setInitScripts(Resource[] initScripts) {
		this.initScripts = initScripts;
	}

	public void setDestroyScripts(Resource[] destroyScripts) {
		this.destroyScripts = destroyScripts;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setIgnoreFailedDrop(boolean ignoreFailedDrop) {
		this.ignoreFailedDrop = ignoreFailedDrop;
	}

}
