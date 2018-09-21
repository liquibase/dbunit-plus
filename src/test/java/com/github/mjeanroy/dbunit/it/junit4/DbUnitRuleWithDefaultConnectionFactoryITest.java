/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2018 Mickael Jeanroy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.mjeanroy.dbunit.it.junit4;

import com.github.mjeanroy.dbunit.core.annotations.DbUnitConnection;
import com.github.mjeanroy.dbunit.core.annotations.DbUnitDataSet;
import com.github.mjeanroy.dbunit.core.annotations.DbUnitInit;
import com.github.mjeanroy.dbunit.integration.junit4.DbUnitRule;
import com.github.mjeanroy.dbunit.it.configuration.DbUnitTest;
import com.github.mjeanroy.dbunit.tests.junit4.HsqldbRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static com.github.mjeanroy.dbunit.tests.db.JdbcQueries.countFrom;
import static org.assertj.core.api.Assertions.assertThat;

@DbUnitTest
@DbUnitConnection(url = "jdbc:hsqldb:mem:testdb", user = "SA", password = "")
@DbUnitInit(sql = "classpath:/sql/init.sql")
public class DbUnitRuleWithDefaultConnectionFactoryITest {

	@ClassRule
	public static HsqldbRule hsqldb = new HsqldbRule(false);

	@Rule
	public DbUnitRule rule = new DbUnitRule();

	@Test
	public void test1() {
		assertThat(countFrom(hsqldb.getConnection(), "foo")).isEqualTo(2);
		assertThat(countFrom(hsqldb.getConnection(), "bar")).isEqualTo(3);
	}

	@Test
	@DbUnitDataSet("/dataset/xml/foo.xml")
	public void test2() {
		assertThat(countFrom(hsqldb.getConnection(), "foo")).isEqualTo(2);
		assertThat(countFrom(hsqldb.getConnection(), "bar")).isEqualTo(0);
	}
}