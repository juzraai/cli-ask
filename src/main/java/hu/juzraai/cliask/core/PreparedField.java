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

	protected PreparedField(Field field, Object object, boolean relevant, String name, Object defaultValue) {
		this.field = field;
		this.object = object;
		this.relevant = relevant;
		this.name = name;
		this.defaultValue = defaultValue;
	}

	public static PreparedField prepare(Field field, Object object) {

		// relevance

		boolean relevant = true;
		try {
			field.setAccessible(true);
			// TODO later: maybe we can check if there's proper converter or @AskRecursively
			relevant = field.isAnnotationPresent(Ask.class)
					&& ((field.getModifiers() & Modifier.FINAL) != Modifier.FINAL);
		} catch (SecurityException e) {
			// TODO LOG
			relevant = false;
		}

		String name = null;
		Object defaultValue = null;
		if (relevant) {

			// name

			Ask ask = field.getAnnotation(Ask.class);
			name = null != ask && ask.value().isEmpty() ? field.getName() : ask.value();

			// default value

			try {
				defaultValue = field.get(object);
			} catch (IllegalAccessException e) {
				// TODO LOG
				relevant = false;
			}
		}

		return new PreparedField(field, object, relevant, name, defaultValue);
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public Field getField() {
		return field;
	}

	public String getName() {
		return name;
	}

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
