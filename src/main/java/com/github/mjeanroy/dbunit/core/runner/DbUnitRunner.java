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

import com.github.mjeanroy.dbunit.core.annotations.DbUnitConfig;
import com.github.mjeanroy.dbunit.core.annotations.DbUnitConfiguration;
import com.github.mjeanroy.dbunit.core.annotations.DbUnitConnection;
import com.github.mjeanroy.dbunit.core.annotations.DbUnitDataSet;
import com.github.mjeanroy.dbunit.core.annotations.DbUnitLiquibase;
import com.github.mjeanroy.dbunit.core.annotations.DbUnitReplacement;
import com.github.mjeanroy.dbunit.core.configuration.DbUnitConfigInterceptor;
import com.github.mjeanroy.dbunit.core.jdbc.JdbcConnectionFactory;
import com.github.mjeanroy.dbunit.core.jdbc.JdbcDataSourceConnectionFactory;
import com.github.mjeanroy.dbunit.core.jdbc.JdbcDefaultConnectionFactory;
import com.github.mjeanroy.dbunit.exception.DbUnitException;
import com.github.mjeanroy.dbunit.exception.JdbcException;
import com.github.mjeanroy.dbunit.loggers.Logger;
import com.github.mjeanroy.dbunit.loggers.Loggers;
import org.dbunit.DefaultDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.github.mjeanroy.dbunit.commons.collections.Collections.forEach;
import static com.github.mjeanroy.dbunit.commons.collections.Collections.map;
import static com.github.mjeanroy.dbunit.commons.io.Io.closeQuietly;
import static com.github.mjeanroy.dbunit.commons.lang.PreConditions.notNull;
import static com.github.mjeanroy.dbunit.commons.reflection.Annotations.findAnnotation;
import static com.github.mjeanroy.dbunit.commons.reflection.Annotations.findStaticFieldAnnotatedWith;
import static com.github.mjeanroy.dbunit.commons.reflection.Annotations.findStaticMethodAnnotatedWith;
import static com.github.mjeanroy.dbunit.commons.reflection.ClassUtils.instantiate;
import static com.github.mjeanroy.dbunit.core.dataset.DataSetFactory.createDataSet;
import static com.github.mjeanroy.dbunit.core.jdbc.JdbcConfiguration.newJdbcConfiguration;

/**
 * Generic class to run DbUnit before/after test method invocation.
 */
public class DbUnitRunner {

	/**
	 * Class Logger.
	 */
	private static final Logger log = Loggers.getLogger(DbUnitRunner.class);

	/**
	 * Test Class.
	 */
	private final Class<?> testClass;

	/**
	 * The test class context, containing initialization context.
	 */
	private final DbUnitClassContext ctx;

	/**
	 * Factory used to retrieve SQL connection before and
	 * after execution of test method.
	 */
	private final JdbcConnectionFactory factory;

	/**
	 * DbUnit data set to load before test method execution.
	 */
	private final IDataSet dataSet;

	/**
	 * Create runner.
	 *
	 * <br>
	 *
	 * DbUnit DataSet will be automatically detected:
	 * <ol>
	 *   <li>If method to launch contains {@link DbUnitDataSet} annotation, it is used.</li>
	 *   <li>If {@link DbUnitDataSet} annotation is not found, a log is displayed, but runner <strong>will not failed.</strong></li>
	 * </ol>
	 *
	 * @param testClass Class to test.
	 * @param factory Factory to get new SQL connection before and after test methods.
	 * @throws DbUnitException If dataSet parsing failed.
	 */
	public DbUnitRunner(Class<?> testClass, JdbcConnectionFactory factory) {
		this.testClass = notNull(testClass, "Test Class must not be null");
		this.factory = notNull(factory, "JDBC Connection Factory must not be null");
		this.dataSet = readDataSet(testClass);
		this.ctx = DbUnitClassContextFactory.from(testClass);

		// Then, run SQL and/or liquibase initialization
		runSqlScript(factory);
		runLiquibase(testClass, factory);
	}

	/**
	 * Create runner.
	 *
	 * @param testClass Class to test.
	 * @param dataSource DataSource to get new SQL connection before and after test methods.
	 * @throws DbUnitException If dataSet parsing failed.
	 * @see #DbUnitRunner
	 */
	public DbUnitRunner(Class<?> testClass, DataSource dataSource) {
		this(testClass, new JdbcDataSourceConnectionFactory(notNull(dataSource, "DataSource must not be null")));
	}

	/**
	 * Create runner and extract the JDBC Connectiob factory from the {@code testClass} that should
	 * be annotated with {@link DbUnitConnection} (or with deprecated {@link DbUnitConfiguration}).
	 *
	 * @param testClass The tested class.
	 */
	public DbUnitRunner(Class<?> testClass) {
		this(testClass, extractJdbcConnectionFactory(testClass));
	}

	/**
	 * Load data set before test execution:
	 * <ol>
	 *   <li>Get new SQL connection.</li>
	 *   <li>Load DataSet and execute setup operation.</li>
	 *   <li>Close SQL connection.</li>
	 * </ol>
	 *
	 * @param testMethod Method to execute.
	 */
	public void beforeTest(Method testMethod) {
		setupOrTearDown(testMethod, SetupDbOperation.getInstance());
	}

	/**
	 * Unload data set after test execution:
	 * <ol>
	 *   <li>Get new SQL connection.</li>
	 *   <li>Remove DataSet and execute tear down operation.</li>
	 *   <li>Close SQL connection.</li>
	 * </ol>
	 *
	 * @param testMethod Executed method.
	 */
	public void afterTest(Method testMethod) {
		setupOrTearDown(testMethod, TearDownDbOperation.getInstance());
	}

	private void setupOrTearDown(Method testMethod, DbOperation op) {
		// Read dataSet from method.
		IDataSet dataSet = readDataSet(testMethod, this.dataSet);
		if (dataSet == null) {
			return;
		}

		Connection connection = factory.getConnection();
		IDatabaseConnection dbConnection = null;

		try {
			log.trace(" 1- Get SQL connection");
			dbConnection = new DatabaseConnection(connection);

			log.trace(" 2- Try to apply DbUnit connection configuration");
			DbUnitConfigInterceptor interceptor = readConfig(testClass, testMethod);
			if (interceptor != null) {
				interceptor.applyConfiguration(dbConnection.getConfig());
			}

			IDatabaseTester dbTester = new DefaultDatabaseTester(dbConnection);

			log.trace(" 3- Load data set");
			dbTester.setDataSet(runReplacements(testClass, dataSet));

			// Apply operation (setup or tear down).
			log.trace(" 4- Apply database operation");
			op.apply(testClass, testMethod, dbTester);

			log.trace(" 5- Closing SQL connection");
			dbConnection.close();
		}
		catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			throw new JdbcException(ex);
		}
		finally {
			if (dbConnection != null) {
				try {
					dbConnection.close();
				}
				catch (SQLException ex) {
					// No Worries.
					log.warn(ex.getMessage());
				}
			}

			closeQuietly(connection);
		}
	}

	/**
	 * Read dbUnit dataSet from class test class annotation.
	 *
	 * @return Parsed dataSet.
	 * @throws DbUnitException If dataSet parsing failed.
	 */
	private static IDataSet readDataSet(Class<?> testClass) {
		DbUnitDataSet annotation = findAnnotation(testClass, null, DbUnitDataSet.class);
		if (annotation != null && annotation.value().length > 0) {
			return readAnnotationDataSet(annotation);
		}

		log.warn("Cannot find @DbUnitDataSet annotation, skip.");
		return null;
	}

	/**
	 * Read DbUnit configuration interceptor, returns {@code null} if no configuration is set.
	 *
	 * @return The interceptor, {@code null} if it is not configured.
	 * @throws DbUnitException If instantiating the interceptor failed.
	 */
	private static DbUnitConfigInterceptor readConfig(Class<?> testClass, Method method) {
		DbUnitConfig annotation = findAnnotation(testClass, method, DbUnitConfig.class);
		if (annotation == null) {
			return null;
		}

		Class<? extends DbUnitConfigInterceptor> interceptor = annotation.value();
		return instantiate(interceptor);
	}

	/**
	 * Read DbUnit from tested method.
	 * If method is not annotated with {@link DbUnitDataSet}, dataSet from
	 * class annotation is returned.
	 *
	 * @param method Tested method.
	 * @param defaultDataSet The default dataSet returned if method is not annotated.
	 * @return DataSet.
	 */
	private static IDataSet readDataSet(Method method, IDataSet defaultDataSet) {
		boolean isAnnotated = method != null && method.isAnnotationPresent(DbUnitDataSet.class);
		return isAnnotated ? readAnnotationDataSet(method.getAnnotation(DbUnitDataSet.class)) : defaultDataSet;
	}

	/**
	 * Find replacements objects and decorate original data set if some are found.
	 *
	 * @param testClass Test class.
	 * @param dataSet Original data set.
	 * @return New data set.
	 */
	private static IDataSet runReplacements(Class<?> testClass, IDataSet dataSet) {
		List<Field> fields = findStaticFieldAnnotatedWith(testClass, DbUnitReplacement.class);
		List<Method> methods = findStaticMethodAnnotatedWith(testClass, DbUnitReplacement.class);
		if (fields.isEmpty() && methods.isEmpty()) {
			return dataSet;
		}

		ReplacementDataSet replacementDataSet = new ReplacementDataSet(dataSet);

		// Apply replacements.
		forEach(fields, new MemberReplacementFunction<Field>(replacementDataSet));
		forEach(methods, new MemberReplacementFunction<Method>(replacementDataSet));

		return replacementDataSet;
	}

	/**
	 * Create dataSet from annotation parameter.
	 *
	 * @param annotation Annotation.
	 * @return DataSet.
	 */
	private static IDataSet readAnnotationDataSet(DbUnitDataSet annotation) {
		if (annotation.value().length == 0) {
			return null;
		}

		try {
			log.debug("Fond @DbUnitDataSet annotation, parse annotation value: {}", (Object[]) annotation.value());
			return createDataSet(annotation.value());
		}
		catch (DataSetException ex) {
			log.error(ex.getMessage(), ex);
			throw new DbUnitException(ex);
		}
	}

	/**
	 * Run SQL initialization script when runner is initialized.
	 * If a script failed, then entire process is stopped and an instance
	 * of {@link DbUnitException} if thrown.
	 *
	 * @param factory The JDBC Connection Factory.
	 */
	private void runSqlScript(JdbcConnectionFactory factory) {
		forEach(ctx.getInitScripts(), new SqlScriptRunnerFunction(factory));
	}

	/**
	 * Run SQL initialization script when runner is initialized.
	 * If a script failed, then entire process is stopped and an instance
	 * of {@link DbUnitException} if thrown.
	 *
	 * @param testClass The tested class.
	 * @param factory The JDBC Connection Factory.
	 */
	private static void runLiquibase(Class<?> testClass, JdbcConnectionFactory factory) {
		DbUnitLiquibase annotation = findAnnotation(testClass, null, DbUnitLiquibase.class);
		if (annotation != null) {
			List<LiquibaseChangeLog> changeLogs = map(annotation.value(), LiquibaseChangeLogMapper.getInstance());
			forEach(changeLogs, new LiquibaseChangeLogUpdaterFunction(factory));
		}
	}

	/**
	 * Extract {@link JdbcConnectionFactory} configuration from test annotated with {@link DbUnitConnection}
	 * or with deprecated {@link DbUnitConfiguration}.
	 *
	 * @param testClass The tested class.
	 * @return The JDBC Connection Factory.
	 */
	private static JdbcConnectionFactory extractJdbcConnectionFactory(Class<?> testClass) {
		DbUnitConfiguration a1 = findAnnotation(testClass, null, DbUnitConfiguration.class);
		DbUnitConnection a2 = findAnnotation(testClass, null, DbUnitConnection.class);
		if (a2 == null && a1 == null) {
			throw new DbUnitException("Cannot find database configuration, please annotate your class with @DbUnitConnection");
		}

		if (a1 != null) {
			log.warn("@DbUnitConfiguration annotation is deprecated and will be removed in a next release, please use @DbUnitConnection instead");
		}

		final String url;
		final String user;
		final String password;

		if (a2 != null) {
			url = a2.url();
			user = a2.user();
			password = a2.password();
		}
		else {
			url = a1.url();
			user = a1.user();
			password = a1.password();
		}

		return new JdbcDefaultConnectionFactory(newJdbcConfiguration(url, user, password));
	}
}
