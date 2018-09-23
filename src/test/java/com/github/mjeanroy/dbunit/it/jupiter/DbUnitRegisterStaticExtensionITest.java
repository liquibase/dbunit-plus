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

package com.github.mjeanroy.dbunit.it.jupiter;

import static com.github.mjeanroy.dbunit.tests.db.JdbcQueries.countFrom;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;

import com.github.mjeanroy.dbunit.core.annotations.DbUnitDataSet;
import com.github.mjeanroy.dbunit.integration.jupiter.DbUnitExtension;
import com.github.mjeanroy.dbunit.it.configuration.DbUnitHsqldbConnection;
import com.github.mjeanroy.dbunit.it.configuration.DbUnitTest;
import com.github.mjeanroy.dbunit.tests.jupiter.HsqldbExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

@ExtendWith(HsqldbExtension.class)
@DbUnitTest
@DbUnitHsqldbConnection
class DbUnitRegisterStaticExtensionITest {

	@RegisterExtension
	static DbUnitExtension extension = new DbUnitExtension();

	@BeforeAll
	static void setup(Connection connection) {
		assertThat(countFrom(connection, "foo")).isZero();
		assertThat(countFrom(connection, "bar")).isZero();
	}

	@Test
	void test1(Connection connection) {
		assertThat(countFrom(connection, "foo")).isEqualTo(2);
		assertThat(countFrom(connection, "bar")).isEqualTo(3);
	}

	@Test
	@DbUnitDataSet("/dataset/xml/foo.xml")
	void test2(EmbeddedDatabase hsqldb) throws Exception {
		assertThat(countFrom(hsqldb.getConnection(), "foo")).isEqualTo(2);
		assertThat(countFrom(hsqldb.getConnection(), "bar")).isEqualTo(0);
	}
}
