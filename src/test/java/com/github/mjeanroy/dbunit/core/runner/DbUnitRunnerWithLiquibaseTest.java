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

package com.github.mjeanroy.dbunit.core.runner;

import com.github.mjeanroy.dbunit.tests.junit4.HsqldbRule;
import com.github.mjeanroy.dbunit.tests.fixtures.WithDataSetAndLiquibase;
import org.junit.ClassRule;
import org.junit.Test;

import java.lang.reflect.Method;

import static com.github.mjeanroy.dbunit.tests.db.JdbcQueries.countFrom;
import static org.assertj.core.api.Assertions.assertThat;

public class DbUnitRunnerWithLiquibaseTest {

	@ClassRule
	public static HsqldbRule hsqldb = new HsqldbRule(false);

	@Test
	public void it_should_execute_sql_script_and_load_data_set() throws Exception {
		Class<WithDataSetAndLiquibase> klass = WithDataSetAndLiquibase.class;
		DbUnitRunner runner = new DbUnitRunner(klass, hsqldb.getDb());

		assertThat(countFrom(hsqldb.getConnection(), "foo")).isZero();
		assertThat(countFrom(hsqldb.getConnection(), "bar")).isZero();

		// Setup Operation
		Method testMethod = klass.getMethod("method1");
		runner.beforeTest(testMethod);

		assertThat(countFrom(hsqldb.getConnection(), "foo")).isEqualTo(2);
		assertThat(countFrom(hsqldb.getConnection(), "bar")).isEqualTo(3);

		// Tear Down Operation
		runner.afterTest(testMethod);

		assertThat(countFrom(hsqldb.getConnection(), "foo")).isZero();
		assertThat(countFrom(hsqldb.getConnection(), "bar")).isZero();
	}
}
