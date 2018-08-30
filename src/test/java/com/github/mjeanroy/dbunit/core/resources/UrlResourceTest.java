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

package com.github.mjeanroy.dbunit.core.resources;

import com.github.mjeanroy.dbunit.tests.builders.UrlBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import static com.github.mjeanroy.dbunit.tests.utils.TestUtils.readStream;
import static com.github.mjeanroy.dbunit.tests.utils.TestUtils.readTestResource;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("SameParameterValue")
public class UrlResourceTest {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort(), false);

	private int port;

	@Before
	public void setUp() {
		port = wireMockRule.port();
	}

	@Test
	public void it_should_return_true_if_file_exists() {
		final String path = "/dataset/json/foo.json";
		final String dataset = readTestResource(path);
		stubFor(WireMock.get(urlEqualTo(path))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/xml")
				.withBody(dataset.trim())));

		final URL url = url(path);
		final UrlResource resource = new UrlResource(url);
		assertThat(resource.exists()).isTrue();
	}

	@Test
	public void it_should_return_false_if_file_does_not_exists() {
		final String path = "/dataset/json/foo.json";
		final URL url = url(path);
		final UrlResource resource = new UrlResource(url);
		assertThat(resource.exists()).isFalse();
	}

	@Test
	public void it_should_return_get_file_name() {
		final String path = "/dataset/json/foo.json";
		final URL url = url(path);
		final UrlResource resource = new UrlResource(url);
		assertThat(resource.getFilename()).isEqualTo("foo.json");
	}

	@Test
	public void it_should_return_false_if_not_directory() {
		final String path = "/dataset/json/foo.json";
		final URL url = url(path);
		final UrlResource resource = new UrlResource(url);
		assertThat(resource.isDirectory()).isFalse();
	}

	@Test
	public void it_should_return_get_file_handler() {
		final String path = "/dataset/json/foo.json";
		final URL url = url(path);
		final UrlResource resource = new UrlResource(url);

		assertThatThrownBy(toFile(resource))
			.isExactlyInstanceOf(UnsupportedOperationException.class)
			.hasMessage(String.format("Resource %s cannot be resolved to absolute file path because it does not reside in the file system", url.toString()));
	}

	@Test
	public void it_should_get_input_stream() throws Exception {
		final String path = "/dataset/json/foo.json";
		final String dataset = readTestResource(path).trim();
		stubFor(WireMock.get(urlEqualTo(path))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/xml")
				.withBody(dataset)));

		final URL url = url(path);
		final UrlResource resource = new UrlResource(url);
		final InputStream stream = resource.openStream();
		final String result = readStream(stream).trim();

		assertThat(result).isEqualTo(dataset);
	}

	@Test
	public void it_should_return_empty_sub_resources() {
		final String path = "/dataset/json/foo.json";
		final String dataset = readTestResource(path).trim();
		stubFor(WireMock.get(urlEqualTo(path))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/xml")
				.withBody(dataset)));

		final URL url = url(path);
		final UrlResource resource = new UrlResource(url);
		final Collection<Resource> subResources = resource.listResources();

		assertThat(subResources)
			.isNotNull()
			.isEmpty();
	}

	@Test
	public void it_should_implement_equals() {
		EqualsVerifier.forClass(UrlResource.class)
			.withNonnullFields("url")
			.withIgnoredFields("scanner")
			.suppress(Warning.STRICT_INHERITANCE)
			.verify();
	}

	@Test
	public void it_should_implement_to_string() {
		final String path = "/dataset/json/foo.json";
		final URL url = url(path);
		final UrlResource r1 = new UrlResource(url);

		assertThat(r1.toString()).isEqualTo(String.format("UrlResource{url: %s}", url.toString()));
	}

	private URL url(String path) {
		return new UrlBuilder()
			.setProtocol("http")
			.setHost("localhost")
			.setPort(port)
			.setPath(path)
			.build();
	}

	private static ThrowingCallable toFile(final UrlResource resource) {
		return new ThrowingCallable() {
			@Override
			public void call() {
				resource.toFile();
			}
		};
	}
}
