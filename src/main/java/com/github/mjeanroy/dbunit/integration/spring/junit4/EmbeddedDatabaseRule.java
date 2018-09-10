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

package com.github.mjeanroy.dbunit.integration.spring.junit4;

import com.github.mjeanroy.dbunit.integration.spring.EmbeddedDatabaseRunner;
import org.junit.rules.ExternalResource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

/**
 * A JUnit 4 {@link org.junit.Rule} that can be used to start/stop embedded database.
 *
 * <p/>
 *
 * Note that:
 *
 * <ul>
 *   <li>When used as a {@link org.junit.ClassRule}, the embedded database will be started before all tests, and stopped after all tests.</li>
 *   <li>When used as a {@link org.junit.Rule}, the embedded database will be started before each test, and stopped after each test.</li>
 * </ul>
 */
public class EmbeddedDatabaseRule extends ExternalResource {

	/**
	 * Instance of {@link EmbeddedDatabase}.
	 */
	private final EmbeddedDatabaseRunner dbRunner;

	/**
	 * Create rule.
	 *
	 * @param db Embedded database.
	 */
	public EmbeddedDatabaseRule(EmbeddedDatabase db) {
		this.dbRunner = new EmbeddedDatabaseRunner(db);
	}

	/**
	 * Create rule with default builder.
	 */
	public EmbeddedDatabaseRule() {
		this.dbRunner = new EmbeddedDatabaseRunner();
	}

	@Override
	protected void before() {
		dbRunner.before();
	}

	@Override
	protected void after() {
		super.after();
		dbRunner.after();
	}

	/**
	 * Gets currently created database instance.
	 *
	 * @return Database instance, may be {@code null} until rule has not been started.
	 */
	public EmbeddedDatabase getDb() {
		return dbRunner.getDb();
	}
}
