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

package com.github.mjeanroy.dbunit.commons.lang;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StringsTest {

	@Test
	public void it_should_check_if_string_is_empty() {
		assertThat(Strings.isEmpty(null)).isTrue();
		assertThat(Strings.isEmpty("")).isTrue();
		assertThat(Strings.isEmpty("foo")).isFalse();
	}

	@Test
	public void it_should_check_if_string_is_not_empty() {
		assertThat(Strings.isNotEmpty("foo")).isTrue();
		assertThat(Strings.isNotEmpty("")).isFalse();
		assertThat(Strings.isNotEmpty(null)).isFalse();
	}

	@Test
	public void it_should_check_if_string_is_blank() {
		assertThat(Strings.isBlank(null)).isTrue();
		assertThat(Strings.isBlank("")).isTrue();
		assertThat(Strings.isBlank("   ")).isTrue();
		assertThat(Strings.isBlank("  foobar  ")).isFalse();
	}
}
