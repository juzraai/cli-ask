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
public class ObjectInspector { // TODO doc

	private final FieldInspector fieldInspector;

	public ObjectInspector(FieldInspector fieldInspector) {
		this.fieldInspector = fieldInspector;
	}

	public FieldInspector getFieldInspector() {
		return fieldInspector;
	}

	@Nonnull
	public List<PreparedField> relevantFieldsOf(@Nonnull Object object) {
		List<PreparedField> fields = new ArrayList<>();
		for (Field field : object.getClass().getDeclaredFields()) {
			PreparedField preparedField = fieldInspector.inspectField(field, object);
			if (preparedField.isRelevant()) {
				fields.add(preparedField);
			}
		}
		return fields;
	}
}
