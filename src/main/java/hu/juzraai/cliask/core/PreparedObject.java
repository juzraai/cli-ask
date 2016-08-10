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
 * Prepares (an object for asking by preparing its fields via {@link
 * PreparedField}'s <code>prepare</code> method, and collecting relevant fields.
 * {@link PreparedObject} contains the input object itself, and it's
 * fields that needs to be asked.
 *
 * @author Zsolt Jurányi
 */
public class PreparedObject {

	private final Object object;
	private final List<PreparedField> fields;

	protected PreparedObject(@Nonnull Object object, @Nonnull List<PreparedField> fields) {
		this.object = object;
		this.fields = fields;
	}

	/**
	 * Prepares (creates interpreted version of) an input object which then
	 * needs to be asked. Calls {@link PreparedField}'s <code>prepare</code>
	 * method to interpret declared fields of the object, then collects relevant
	 * fields. Finally produces a {@link PreparedObject} containing all
	 * necessary data for {@link AskFor}.
	 *
	 * @param object The input object which needs to be prepared for asking
	 * @return A prepared object containing the input object and it's relevant
	 * fields as {@link PreparedField}s
	 */
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

		return new PreparedObject(object, fields);
	}

	/**
	 * @return All relevant fields of input object as {@link PreparedField}s
	 */
	@Nonnull
	public List<PreparedField> getFields() {
		return fields;
	}

	/**
	 * @return The input objects
	 */
	@Nonnull
	public Object getObject() {
		return object;
	}
}
