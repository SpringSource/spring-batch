/*
 * Copyright 2006-2016 the original author or authors.
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
package org.springframework.batch.item.database.support;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Risberg
 * @author Michael Minella
 */
public class SqlWindowingPagingQueryProviderTests extends AbstractSqlPagingQueryProviderTests {

	public SqlWindowingPagingQueryProviderTests() {
		pagingQueryProvider = new SqlWindowingPagingQueryProvider();
	}

	@Test 
	@Override
	public void testGenerateFirstPageQuery() {
		String sql = "SELECT * FROM ( SELECT *, ROW_NUMBER() OVER ( ORDER BY id ASC) AS ROW_NUMBER FROM foo WHERE bar = 1) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER <= 100 ORDER BY id ASC";
		String s = pagingQueryProvider.generateFirstPageQuery(pageSize);
		assertEquals("", sql, s);
	}

	@Test 
	@Override
	public void testGenerateRemainingPagesQuery() {
		String sql = "SELECT * FROM ( SELECT *, ROW_NUMBER() OVER ( ORDER BY id ASC) AS ROW_NUMBER FROM foo WHERE bar = 1) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER <= 100 AND ((id > ?)) ORDER BY id ASC";
		String s = pagingQueryProvider.generateRemainingPagesQuery(pageSize);
		assertEquals("", sql, s);
	}

	@Test
	@Override
	public void testGenerateFirstPageQueryWithGroupBy() {
		pagingQueryProvider.setGroupClause("dep");
		String sql = "SELECT * FROM ( SELECT *, ROW_NUMBER() OVER ( ORDER BY id ASC) AS ROW_NUMBER FROM foo WHERE bar = 1 GROUP BY dep) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER <= 100 ORDER BY id ASC";
		String s = pagingQueryProvider.generateFirstPageQuery(pageSize);
		assertEquals(sql, s);
	}

	@Test
	@Override
	public void testGenerateRemainingPagesQueryWithGroupBy() {
		pagingQueryProvider.setGroupClause("dep");
		String sql = "SELECT * FROM ( SELECT *, ROW_NUMBER() OVER ( ORDER BY id ASC) AS ROW_NUMBER FROM foo WHERE bar = 1 GROUP BY dep) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER <= 100 AND ((id > ?)) ORDER BY id ASC";
		String s = pagingQueryProvider.generateRemainingPagesQuery(pageSize);
		assertEquals(sql, s);
	}

	@Override
	public String getFirstPageSqlWithMultipleSortKeys() {
		return "SELECT * FROM ( SELECT *, ROW_NUMBER() OVER ( ORDER BY name ASC, id DESC) AS ROW_NUMBER FROM foo WHERE bar = 1) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER <= 100 ORDER BY name ASC, id DESC";
	}

	@Override
	public String getRemainingSqlWithMultipleSortKeys() {
		return "SELECT * FROM ( SELECT *, ROW_NUMBER() OVER ( ORDER BY name ASC, id DESC) AS ROW_NUMBER FROM foo WHERE bar = 1) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER <= 100 AND ((name > ?) OR (name = ? AND id < ?)) ORDER BY name ASC, id DESC";
	}

}
