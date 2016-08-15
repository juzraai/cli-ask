/*
 * Copyright 2016 Zsolt Jurányi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.juzraai.cliask.annotation;

import hu.juzraai.cliask.convert.ConvertTo;
import hu.juzraai.cliask.convert.Converters;
import hu.juzraai.cliask.convert.DefaultConverter;
import hu.juzraai.cliask.core.AskFor;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used on fields of a POJO class to tell CLI-Ask ({@link
 * AskFor}.object(Object)) that the field is relevant for asking. Fields that
 * does not have this annotation will be skipped.
 * <p>
 * Also this annotation can add extra per-field configuration to {@link
 * AskFor}'s behaviour.
 * <p>
 * The default <code>value</code> attribute stands for the label to be printed
 * out in front of the input cursor.
 *
 * @author Zsolt Jurányi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Ask {

	/**
	 * {@link AskFor} calls {@link Converters} to convert the raw input string
	 * into the type of the field. By default, the converter is chosen by {@link
	 * Converters}, but you can override it by specifying a converter class in
	 * this attribute.
	 *
	 * @return Converter class to be used; if it's {@link DefaultConverter} it
	 * means the default converter selector algorithm will be used
	 */
	@Nonnull
	Class<? extends ConvertTo<?>> converter() default DefaultConverter.class;

	/**
	 * Label to be printed out in front of input cursor. If you leave it empty,
	 * the name of the field will be used.
	 *
	 * @return Label to be printed out in front of input cursor; if it's empty,
	 * the name of the field will be used
	 */
	@Nonnull
	String value() default "";
}
