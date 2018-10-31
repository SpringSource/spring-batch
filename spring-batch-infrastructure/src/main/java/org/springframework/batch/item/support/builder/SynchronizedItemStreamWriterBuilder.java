/*
 * Copyright 2018 the original author or authors.
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
package org.springframework.batch.item.support.builder;

import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.support.SynchronizedItemStreamWriter;
import org.springframework.util.Assert;

/**
 * Creates a fully qualified {@link SynchronizedItemStreamWriter}.
 *
 * @author Dimitrios Liapis
 */
public class SynchronizedItemStreamWriterBuilder<T> {

	private ItemStreamWriter<T> delegate;

	public SynchronizedItemStreamWriterBuilder<T> delegate(ItemStreamWriter<T> delegate) {
		this.delegate = delegate;

		return this;
	}

	public SynchronizedItemStreamWriter<T> build() {
		Assert.notNull(this.delegate, "A delegate is required");

		SynchronizedItemStreamWriter<T> writer = new SynchronizedItemStreamWriter<>();
		writer.setDelegate(this.delegate);
		return writer;
	}
}
