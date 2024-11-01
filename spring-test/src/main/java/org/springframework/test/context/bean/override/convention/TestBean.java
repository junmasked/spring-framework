/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.test.context.bean.override.convention;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.bean.override.BeanOverride;

/**
 * {@code @TestBean} is an annotation that can be applied to a field in a test
 * class to override a bean in the test's
 * {@link org.springframework.context.ApplicationContext ApplicationContext}
 * using a static factory method.
 *
 * <p>By default, the bean to override is inferred from the type of the annotated
 * field. If multiple candidates exist, a {@code @Qualifier} annotation can be
 * used to help disambiguate. In the absence of a {@code @Qualifier} annotation,
 * the name of the annotated field will be used as a fallback qualifier.
 * Alternatively, you can explicitly specify a bean name to replace by setting the
 * {@link #value() value} or {@link #name() name} attribute.
 *
 * <p>A bean will be created if a corresponding bean does not exist. However, if
 * you would like for the test to fail when a corresponding bean does not exist,
 * you can set the {@link #enforceOverride() enforceOverride} attribute to {@code true}
 * &mdash; for example,  {@code @TestBean(enforceOverride = true)}.
 *
 * <p>The instance is created from a zero-argument static factory method in the
 * test class whose return type is compatible with the annotated field. In the
 * case of a nested test class, the enclosing class hierarchy is also searched.
 * Similarly, if the test class extends from a base class or implements any
 * interfaces, the entire type hierarchy is searched. Alternatively, a factory
 * method in an external class can be referenced via its fully-qualified method
 * name following the syntax {@code <fully-qualified class name>#<method name>}
 * &mdash; for example,
 * {@code @TestBean(methodName = "org.example.TestUtils#createCustomerRepository")}.
 *
 * <p>The factory method is deduced as follows.
 *
 * <ul>
 * <li>If the {@link #methodName()} is specified, look for a static method with
 * that name.</li>
 * <li>If a method name is not specified, look for exactly one static method
 * named with either the name of the annotated field or the name of the bean
 * (if specified).</li>
 * </ul>
 *
 * <p>Consider the following example.
 *
 * <pre><code> class CustomerServiceTests {
 *
 *     &#064;TestBean
 *     private CustomerRepository repository;
 *
 *     // &#064;Test methods ...
 *
 *     private static CustomerRepository repository() {
 *         return new TestCustomerRepository();
 *     }
 * }</code></pre>
 *
 * <p>In the example above, the {@code repository} bean is replaced by the
 * instance generated by the {@code repository()} method. Not only is the
 * overridden instance injected into the {@code repository} field, but it is
 * also replaced in the {@code BeanFactory} so that other injection points for
 * that bean use the overridden bean instance.
 *
 * <p>To make things more explicit, the bean and method names can be set,
 * as shown in the following example.
 *
 * <pre><code> class CustomerServiceTests {
 *
 *     &#064;TestBean(name = "customerRepository", methodName = "createTestCustomerRepository")
 *     CustomerRepository repository;
 *
 *     // &#064;Test methods ...
 *
 *     static CustomerRepository createTestCustomerRepository() {
 *         return new TestCustomerRepository();
 *     }
 * }</code></pre>
 *
 * <p><strong>NOTE</strong>: Only <em>singleton</em> beans can be overridden.
 * Any attempt to override a non-singleton bean will result in an exception. When
 * overriding a bean created by a {@link org.springframework.beans.factory.FactoryBean
 * FactoryBean}, the {@code FactoryBean} will be replaced with a singleton bean
 * corresponding to the value returned from the {@code @TestBean} factory method.
 *
 * @author Simon Baslé
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 6.2
 * @see org.springframework.test.context.bean.override.mockito.MockitoBean @MockitoBean
 * @see org.springframework.test.context.bean.override.mockito.MockitoSpyBean @MockitoSpyBean
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BeanOverride(TestBeanOverrideProcessor.class)
public @interface TestBean {

	/**
	 * Alias for {@link #name()}.
	 * <p>Intended to be used when no other attributes are needed &mdash; for
	 * example, {@code @TestBean("customBeanName")}.
	 * @see #name()
	 */
	@AliasFor("name")
	String value() default "";

	/**
	 * Name of the bean to override.
	 * <p>If left unspecified, the bean to override is selected according to
	 * the annotated field's type, taking qualifiers into account if necessary.
	 * See the {@linkplain TestBean class-level documentation} for details.
	 * @see #value()
	 */
	@AliasFor("value")
	String name() default "";

	/**
	 * Name of the static factory method that will be used to instantiate the bean
	 * to override.
	 * <p>A search will be performed to find the factory method in the test class,
	 * in one of its superclasses, or in any implemented interfaces. In the case
	 * of a nested test class, the enclosing class hierarchy will also be searched.
	 * <p>Alternatively, a factory method in an external class can be referenced
	 * via its fully-qualified method name following the syntax
	 * {@code <fully-qualified class name>#<method name>} &mdash; for example,
	 * {@code @TestBean(methodName = "org.example.TestUtils#createCustomerRepository")}.
	 * <p>If left unspecified, the name of the factory method will be detected
	 * based either on the name of the annotated field or the name of the bean.
	 */
	String methodName() default "";

	/**
	 * Whether to require the existence of the bean being overridden.
	 * <p>Defaults to {@code false} which means that a bean will be created if a
	 * corresponding bean does not exist.
	 * <p>Set to {@code true} to cause an exception to be thrown if a corresponding
	 * bean does not exist.
	 * @see org.springframework.test.context.bean.override.BeanOverrideStrategy#REPLACE_OR_CREATE
	 * @see org.springframework.test.context.bean.override.BeanOverrideStrategy#REPLACE
	 */
	boolean enforceOverride() default false;

}
