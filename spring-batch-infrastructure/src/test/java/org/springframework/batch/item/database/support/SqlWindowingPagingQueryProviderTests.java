/*
 * Copyright 2006-2012 the original author or authors.
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

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Thomas Risberg
 * @author Michael Minella
 */
public class SqlWindowingPagingQueryProviderTests extends AbstractSqlPagingQueryProviderTests {

	public SqlWindowingPagingQueryProviderTests() {
		pagingQueryProvider = new SqlWindowingPagingQueryProvider();
	}

	@Test @Override
	public void testGenerateFirstPageQuery() {
		String sql = "SELECT * FROM ( SELECT id, name, age, ROW_NUMBER() OVER ( ORDER BY id ASC) AS ROW_NUMBER FROM foo WHERE bar = 1) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER <= 100";
		String s = pagingQueryProvider.generateFirstPageQuery(pageSize);
		assertEquals("", sql, s);
	}

	@Test @Override
	public void testGenerateRemainingPagesQuery() {
		String sql = "SELECT * FROM ( SELECT id, name, age, ROW_NUMBER() OVER ( ORDER BY id ASC) AS ROW_NUMBER FROM foo WHERE bar = 1 AND ((id > ?))) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER <= 100";
		String s = pagingQueryProvider.generateRemainingPagesQuery(pageSize);
		assertEquals("", sql, s);
	}

	@Test @Override
	public void testGenerateJumpToItemQuery() {
		String sql = "SELECT id FROM ( SELECT id, ROW_NUMBER() OVER ( ORDER BY id ASC) AS ROW_NUMBER FROM foo WHERE bar = 1) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER = 100";
		String s = pagingQueryProvider.generateJumpToItemQuery(145, pageSize);
		assertEquals("", sql, s);
	}

	@Test @Override
	public void testGenerateJumpToItemQueryForFirstPage() {
		String sql = "SELECT id FROM ( SELECT id, ROW_NUMBER() OVER ( ORDER BY id ASC) AS ROW_NUMBER FROM foo WHERE bar = 1) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER = 1";
		String s = pagingQueryProvider.generateJumpToItemQuery(45, pageSize);
		Assert.assertEquals("", sql, s);
	}

	@Override
	@Test
	public void testGenerateFirstPageQueryWithGroupBy() {
		pagingQueryProvider.setGroupClause("dep");
		String sql = "SELECT * FROM ( SELECT id, name, age, ROW_NUMBER() OVER ( ORDER BY id ASC) AS ROW_NUMBER FROM foo WHERE bar = 1 GROUP BY dep) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER <= 100";
		String s = pagingQueryProvider.generateFirstPageQuery(pageSize);
		assertEquals(sql, s);
	}

	@Override
	@Test
	public void testGenerateRemainingPagesQueryWithGroupBy() {
		pagingQueryProvider.setGroupClause("dep");
		String sql = "SELECT * FROM ( SELECT id, name, age, ROW_NUMBER() OVER ( ORDER BY id ASC) AS ROW_NUMBER FROM foo WHERE bar = 1 AND ((id > ?)) GROUP BY dep) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER <= 100";
		String s = pagingQueryProvider.generateRemainingPagesQuery(pageSize);
		assertEquals(sql, s);
	}

	@Override
	@Test
	public void testGenerateJumpToItemQueryWithGroupBy() {
		pagingQueryProvider.setGroupClause("dep");
		String sql = "SELECT id FROM ( SELECT id, ROW_NUMBER() OVER ( ORDER BY id ASC) AS ROW_NUMBER FROM foo WHERE bar = 1 GROUP BY dep) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER = 100";
		String s = pagingQueryProvider.generateJumpToItemQuery(145, pageSize);
		assertEquals(sql, s);
	}

	@Override
	@Test
	public void testGenerateJumpToItemQueryForFirstPageWithGroupBy() {
		pagingQueryProvider.setGroupClause("dep");
		String sql = "SELECT id FROM ( SELECT id, ROW_NUMBER() OVER ( ORDER BY id ASC) AS ROW_NUMBER FROM foo WHERE bar = 1 GROUP BY dep) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER = 1";
		String s = pagingQueryProvider.generateJumpToItemQuery(45, pageSize);
		assertEquals(sql, s);
	}

	@Override
	@Test
	public void testGenerateFirstPageQueryWithMultipleSortKeys() {
		Map<String, Boolean> sortKeys = new LinkedHashMap<String, Boolean>();
		sortKeys.put("id", true);
		sortKeys.put("name", false);
		pagingQueryProvider.setSortKeys(sortKeys);
		String sql = "SELECT * FROM ( SELECT id, name, age, ROW_NUMBER() OVER ( ORDER BY id ASC, name DESC) AS ROW_NUMBER FROM foo WHERE bar = 1) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER <= 100";
		String s = pagingQueryProvider.generateFirstPageQuery(pageSize);
		assertEquals("", sql, s);
	}

	@Override
	@Test
	public void testGenerateRemainingPagesQueryWithMultipleSortKeys() {
		Map<String, Boolean> sortKeys = new LinkedHashMap<String, Boolean>();
		sortKeys.put("id", true);
		sortKeys.put("name", false);
		pagingQueryProvider.setSortKeys(sortKeys);
		String sql = "SELECT * FROM ( SELECT id, name, age, ROW_NUMBER() OVER ( ORDER BY id ASC, name DESC) AS ROW_NUMBER FROM foo WHERE bar = 1 AND ((id > ?) OR (id = ? AND name < ?))) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER <= 100";
		String s = pagingQueryProvider.generateRemainingPagesQuery(pageSize);
		assertEquals("", sql, s);
	}

	@Override
	@Test
	public void testGenerateJumpToItemQueryWithMultipleSortKeys() {
		Map<String, Boolean> sortKeys = new LinkedHashMap<String, Boolean>();
		sortKeys.put("id", true);
		sortKeys.put("name", false);
		pagingQueryProvider.setSortKeys(sortKeys);
		String sql = "SELECT id, name FROM ( SELECT id, name, ROW_NUMBER() OVER ( ORDER BY id ASC, name DESC) AS ROW_NUMBER FROM foo WHERE bar = 1) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER = 100";
		String s = pagingQueryProvider.generateJumpToItemQuery(145, pageSize);
		assertEquals("", sql, s);
	}

	@Override
	@Test
	public void testGenerateJumpToItemQueryForFirstPageWithMultipleSortKeys() {
		Map<String, Boolean> sortKeys = new LinkedHashMap<String, Boolean>();
		sortKeys.put("id", true);
		sortKeys.put("name", false);
		pagingQueryProvider.setSortKeys(sortKeys);
		String sql = "SELECT id, name FROM ( SELECT id, name, ROW_NUMBER() OVER ( ORDER BY id ASC, name DESC) AS ROW_NUMBER FROM foo WHERE bar = 1) AS TMP_SUB WHERE TMP_SUB.ROW_NUMBER = 1";
		String s = pagingQueryProvider.generateJumpToItemQuery(45, pageSize);
		Assert.assertEquals("", sql, s);
	}
}
