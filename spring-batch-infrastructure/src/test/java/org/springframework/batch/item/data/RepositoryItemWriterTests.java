/*
 * Copyright 2013-2014 the original author or authors.
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
package org.springframework.batch.item.data;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.repository.CrudRepository;

public class RepositoryItemWriterTests {

	@Mock
	private CrudRepository<String, Serializable> repository;

	private RepositoryItemWriter<String> writer;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		writer = new RepositoryItemWriter<String>();
		writer.setMethodName("save");
		writer.setRepository(repository);
	}

	@Test
	public void testAfterPropertiesSet() throws Exception {
		writer.afterPropertiesSet();

		writer.setRepository(null);

		try {
			writer.afterPropertiesSet();
			fail();
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testWriteNoItems() throws Exception {
		writer.write(null);

		writer.write(new ArrayList<String>());

		verifyZeroInteractions(repository);
	}

	@Test
	public void testWriteItems() throws Exception {
		List<String> items = Collections.singletonList("foo");

		writer.write(items);

		verify(repository).save("foo");
	}
}
