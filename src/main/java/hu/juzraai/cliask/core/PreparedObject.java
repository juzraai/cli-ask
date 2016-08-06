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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zsolt Jurányi
 */
public class PreparedObject {

	private final Class<?> clazz;
	private final Object object;
	private final String name;
	private final List<PreparedField> fields;

	protected PreparedObject(Class<?> clazz, Object object, String name, List<PreparedField> fields) {
		this.clazz = clazz;
		this.object = object;
		this.name = name;
		this.fields = fields;
	}

	public static PreparedObject prepare(Object object) {
		Class<?> clazz = object.getClass();

		// name

		Ask ask = clazz.getAnnotation(Ask.class);
		String name = null != ask && !ask.value().isEmpty() ? ask.value() : "";

		// fields

		List<PreparedField> fields = new ArrayList<>();
		for (Field field : clazz.getDeclaredFields()) {
			PreparedField preparedField = PreparedField.prepare(field, object);
			if (preparedField.isRelevant()) {
				fields.add(preparedField);
			}
		}

		return new PreparedObject(clazz, object, name, fields);
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public List<PreparedField> getFields() {
		return fields;
	}

	public String getName() {
		return name;
	}

	public Object getObject() {
		return object;
	}


}
