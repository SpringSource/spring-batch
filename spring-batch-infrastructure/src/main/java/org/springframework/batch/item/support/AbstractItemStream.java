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
package org.springframework.batch.item.support;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;


/**
 * Empty method implementation of {@link ItemStream}.
 *
 * @author Dave Syer
 *
 */
public abstract class AbstractItemStream implements ItemStream {

	/**
	 * No-op.
	 * @see org.springframework.batch.item.ItemStream#close()
	 */
    @Override
	public void close() throws ItemStreamException {
	}

	/**
	 * No-op.
	 * @see org.springframework.batch.item.ItemStream#open(ExecutionContext)
	 */
    @Override
	public void open(ExecutionContext executionContext) throws ItemStreamException {
	}

	/**
	 * Return empty {@link ExecutionContext}.
	 * @see org.springframework.batch.item.ItemStream#update(ExecutionContext)
	 */
    @Override
	public void update(ExecutionContext executionContext) {
	}

}
