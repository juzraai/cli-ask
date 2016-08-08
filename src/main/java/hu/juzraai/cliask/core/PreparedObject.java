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

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zsolt Jurányi
 */
public class PreparedObject { // TODO doc

	private final Class<?> clazz;
	private final Object object;
	private final List<PreparedField> fields;

	protected PreparedObject(@Nonnull Class<?> clazz, @Nonnull Object object, @Nonnull List<PreparedField> fields) {
		this.clazz = clazz;
		this.object = object;
		this.fields = fields;
	}

	@Nonnull
	public static PreparedObject prepare(@Nonnull Object object) {
		Class<?> clazz = object.getClass();

		List<PreparedField> fields = new ArrayList<>();
		for (Field field : clazz.getDeclaredFields()) {
			PreparedField preparedField = PreparedField.prepare(field, object);
			if (preparedField.isRelevant()) {
				fields.add(preparedField);
			}
		}

		return new PreparedObject(clazz, object, fields);
	}

	@Nonnull
	public Class<?> getClazz() {
		return clazz;
	}

	@Nonnull
	public List<PreparedField> getFields() {
		return fields;
	}

	@Nonnull
	public Object getObject() {
		return object;
	}
}
