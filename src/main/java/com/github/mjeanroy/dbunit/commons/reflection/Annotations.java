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

package com.github.mjeanroy.dbunit.commons.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static com.github.mjeanroy.dbunit.commons.collections.Collections.filter;
import static com.github.mjeanroy.dbunit.commons.reflection.Reflections.findStaticFields;
import static com.github.mjeanroy.dbunit.commons.reflection.Reflections.findStaticMethods;
import static java.util.Arrays.asList;

/**
 * Static Annotation Utilities.
 */
public final class Annotations {

	/**
	 * The list of packages that should not be scanned for meta-annotation (since these packages may not
	 * contains annotations of DbUnit+!).
	 */
	private static final List<String> BLACKLISTED_PACKAGES = asList(
		"java.lang.",
		"org.junit."
	);

	// Ensure non instantiation.
	private Annotations() {
	}

	/**
	 * Find expected annotation on method, if annotation is defined.
	 *
	 * @param method The method.
	 * @param annotationClass Annotation class to look for.
	 * @param <T> Type of annotation.
	 * @return Annotation if found, {@code null} otherwise.
	 */
	public static <T extends Annotation> T findAnnotation(Method method, Class<T> annotationClass) {
		return findAnnotationOn(method, annotationClass);
	}

	/**
	 * Find expected annotation on:
	 * <ul>
	 * <li>Method if annotation is defined.</li>
	 * <li>Class if annotation is defined.</li>
	 * </ul>
	 *
	 * @param klass Class.
	 * @param method Method in given {@code class}.
	 * @param annotationClass Annotation class to look for.
	 * @param <T> Type of annotation.
	 * @return Annotation if found, {@code null} otherwise.
	 */
	public static <T extends Annotation> T findAnnotation(Class<?> klass, Method method, Class<T> annotationClass) {
		// First, search on method.
		if (method != null) {
			T annotation = findAnnotation(method, annotationClass);
			if (annotation != null) {
				return annotation;
			}
		}

		// Then, search on class.
		return findAnnotation(klass, annotationClass);
	}

	/**
	 * Find expected annotation on class, or a class in the hierarchy.
	 *
	 * @param klass Class.
	 * @param annotationClass Annotation class to look for.
	 * @param <T> Type of annotation.
	 * @return Annotation if found, {@code null} otherwise.
	 */
	public static <T extends Annotation> T findAnnotation(Class<?> klass, Class<T> annotationClass) {
		return findAnnotationOn(klass, annotationClass);
	}

	/**
	 * Find expected annotation on given element.
	 *
	 * @param element Class.
	 * @param annotationClass Annotation class to look for.
	 * @param <T> Type of annotation.
	 * @return Annotation if found, {@code null} otherwise.
	 */
	static <T extends Annotation> T findAnnotationOn(AnnotatedElement element, Class<T> annotationClass) {
		if (element == null) {
			return null;
		}

		// Is it directly present?
		if (element.isAnnotationPresent(annotationClass)) {
			return element.getAnnotation(annotationClass);
		}

		// Search for meta-annotation
		for (Annotation candidate : element.getAnnotations()) {
			Class<? extends Annotation> candidateAnnotationType = candidate.annotationType();
			if (shouldScan(candidateAnnotationType)) {
				T result = findAnnotation(candidateAnnotationType, annotationClass);
				if (result != null) {
					return result;
				}
			}
		}

		if (element instanceof Class) {
			Class<?> klass = (Class<?>) element;

			// Look on interfaces.
			for (Class<?> intf : klass.getInterfaces()) {
				if (shouldScan(intf)) {
					T result = findAnnotationOn(intf, annotationClass);
					if (result != null) {
						return result;
					}
				}
			}

			// Go up in the class hierarchy.
			Class<?> superClass = klass.getSuperclass();
			if (shouldScan(superClass)) {
				return findAnnotation(superClass, annotationClass);
			}
		}

		return null;
	}

	/**
	 * Get all static fields annotated with given annotation.
	 *
	 * @param klass Class to analyze.
	 * @param annotation Annotation to look for.
	 * @param <T> Type of annotation.
	 * @return List of fields annotated with given annotation.
	 */
	public static <T extends Annotation> List<Field> findStaticFieldAnnotatedWith(Class<?> klass, Class<T> annotation) {
		List<Field> fields = findStaticFields(klass);
		return filter(fields, new MemberAnnotatedWithPredicate<Field, T>(annotation));
	}

	/**
	 * Get all static fields annotated with given annotation.
	 *
	 * @param klass Class to analyze.
	 * @param annotation Annotation to look for.
	 * @param <T> Type of annotation.
	 * @return List of fields annotated with given annotation.
	 */
	public static <T extends Annotation> List<Method> findStaticMethodAnnotatedWith(Class<?> klass, Class<T> annotation) {
		List<Method> fields = findStaticMethods(klass);
		return filter(fields, new MemberAnnotatedWithPredicate<Method, T>(annotation));
	}

	/**
	 * Check if it is worth scanning this element (i.e there is a chance to find useful
	 * annotation).
	 *
	 * @param elementType The element class type.
	 * @return {@code true} if element should be scanned, {@code false} otherwise.
	 */
	private static boolean shouldScan(Class<?> elementType) {
		if (elementType == null) {
			return false;
		}

		String name = elementType.getName();

		for (String pkg : BLACKLISTED_PACKAGES) {
			if (name.startsWith(pkg)) {
				return false;
			}
		}

		return true;
	}
}
