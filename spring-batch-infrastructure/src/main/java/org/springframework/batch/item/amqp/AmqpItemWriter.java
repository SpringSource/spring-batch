/*
 * Copyright 2012 the original author or authors.
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

package org.springframework.batch.item.amqp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.batch.item.ItemWriter;
import org.springframework.util.Assert;

import java.util.List;

/**
 * <p>
 * AMQP {@link ItemWriter} implementation using an {@link AmqpTemplate} to
 * send messages. Messages will be sent to the nameless exchange if not specified
 * on the provided {@link AmqpTemplate}.
 * </p>
 *
 * @author Chris Schaefer
 * @author Anand Hemmige
 */
public class AmqpItemWriter<T> implements ItemWriter<T> {
    private final AmqpTemplate amqpTemplate;
    private final String routingKey;
    private final String exchange;

    private final Log log = LogFactory.getLog(getClass());

    public AmqpItemWriter(final AmqpTemplate amqpTemplate) {
        Assert.notNull(amqpTemplate, "AmpqTemplate must not be null");

        this.amqpTemplate = amqpTemplate;
        this.routingKey = null;
        this.exchange = null;
    }

    public AmqpItemWriter(final AmqpTemplate amqpTemplate, final String routingKey) {
        Assert.notNull(amqpTemplate, "AmpqTemplate must not be null");
        Assert.notNull(routingKey, "routingKey must not be null");

        this.amqpTemplate = amqpTemplate;
        this.routingKey = routingKey;
        this.exchange = null;
    }

    public AmqpItemWriter(final AmqpTemplate amqpTemplate, final String routingKey, final String exchange) {
        Assert.notNull(amqpTemplate, "AmpqTemplate must not be null");
        Assert.notNull(routingKey, "routingKey must not be null");
        Assert.notNull(exchange, "exchange must not be null");

        this.amqpTemplate = amqpTemplate;
        this.routingKey = routingKey;
        this.exchange = exchange;
    }


    @Override
    public void write(final List<? extends T> items) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Writing to AMQP with " + items.size() + " items.");
        }

        for (T item : items) {
            if (routingKey != null && exchange != null) {
                amqpTemplate.convertAndSend(exchange, routingKey, item);
            } else if (routingKey != null) {
                amqpTemplate.convertAndSend(routingKey, item);
            } else {
                amqpTemplate.convertAndSend(item);
            }
        }
    }
}
