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

package hu.juzraai.cliask.core;

import hu.juzraai.cliask.annotation.Ask;
import hu.juzraai.cliask.annotation.UseConverter;
import hu.juzraai.cliask.convert.ConvertTo;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author Zsolt Jurányi
 */
public class PreparedField {

	private final Field field;
	private final Object object;
	private final boolean relevant;
	private final String name;
	private final Object defaultValue;
	private final ConvertTo<?> converter;

	protected PreparedField(@Nonnull Field field, @Nonnull Object object, boolean relevant, @Nonnull String name, Object defaultValue, ConvertTo<?> converter) {
		this.field = field;
		this.object = object;
		this.relevant = relevant;
		this.name = name;
		this.defaultValue = defaultValue;
		this.converter = converter;
	}

	@Nonnull
	public static PreparedField prepare(@Nonnull Field field, @Nonnull Object object) {
		boolean relevant = true;
		String name = field.getName();
		Object defaultValue = null;
		ConvertTo<?> converter = null;

		try {

			// relevance
			Ask ask = field.getAnnotation(Ask.class);
			relevant = null != ask && ((field.getModifiers() & Modifier.FINAL) != Modifier.FINAL);
			// TODO later: maybe we can check if there's proper converter or @AskRecursively

			if (relevant) {
				field.setAccessible(true); // throws SE

				// name
				if (!ask.value().isEmpty()) {
					name = ask.value();
				}

				// default value
				defaultValue = field.get(object); // throws IAE

				// converter

				UseConverter useConverter = field.getAnnotation(UseConverter.class);
				if (null != useConverter) {
					converter = useConverter.value().newInstance(); // throws IE, IAE
				}
			}
		} catch (SecurityException | InstantiationException | IllegalAccessException e) {
			// TODO LOG
			relevant = false;
		}

		return new PreparedField(field, object, relevant, name, defaultValue, converter);
	}

	public ConvertTo<?> getConverter() {
		return converter;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	@Nonnull
	public Field getField() {
		return field;
	}

	@Nonnull
	public String getName() {
		return name;
	}

	@Nonnull
	public Object getObject() {
		return object;
	}

	public boolean isRelevant() {
		return relevant;
	}

	public void set(Object value) {
		if (relevant) {
			try {
				field.set(object, value);
			} catch (IllegalAccessException e) {
				// TODO LOG
			}
		} else {
			throw new IllegalStateException("set() called on irrelevant PreparedField");
		}
	}
}
