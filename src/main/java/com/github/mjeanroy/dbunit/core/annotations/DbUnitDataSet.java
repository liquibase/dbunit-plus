/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Mickael Jeanroy
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

package com.github.mjeanroy.dbunit.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Data set annotation, used to specify which data set should be loaded for
 * the next test.
 *
 * This annotation can be used on:
 * <ul>
 *   <li>Method (i.e test method).</li>
 *   <li>Class (i.e test class).</li>
 *   <li>Package (i.e package where test classes belongs)</li>
 * </ul>
 *
 * For example:
 *
 * <pre><code>
 *
 *  @DbUnitDataSet("/dataset/xml")
 *   public class TestClass {
 *     @Rule
 *     public DbUnitRule rule = new DbUnitRule(connectionFactory);
 *
 *     @Test
 *     public void test1() {
 *     }
 *
 *     @Test
 *     @DbUnitDataSet("/dataset/xml/table1.xml")
 *     public void test2() {
 *     }
 *   }
 *
 * </code></pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({
	ElementType.METHOD,
	ElementType.TYPE,
	ElementType.PACKAGE
})
public @interface DbUnitDataSet {

	/**
	 * Set of data set file to load.
	 *
	 * @return DataSet file to load.
	 */
	String[] value() default {};
}