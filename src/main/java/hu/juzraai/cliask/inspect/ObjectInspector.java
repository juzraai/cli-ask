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

package hu.juzraai.cliask.inspect;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains method to inspect a POJO. Inspecting means that it analyzes the
 * object using Reflection and returns with prepared values which then be used
 * when asking for values.
 *
 * @author Zsolt Jurányi
 */
public class ObjectInspector {

	private final FieldInspector fieldInspector;

	/**
	 * Creates a new instance.
	 *
	 * @param fieldInspector Field inspector utility to be used to analyze and
	 *                       prepare fields of input objects
	 */
	public ObjectInspector(FieldInspector fieldInspector) {
		this.fieldInspector = fieldInspector;
	}

	/**
	 * @return Field inspector utility to be used to analyze and prepare fields
	 * of input objects
	 */
	public FieldInspector getFieldInspector() {
		return fieldInspector;
	}

	/**
	 * Iterates through the declared fields of the given object and uses the
	 * {@link FieldInspector} to analyze them. Returns a list which contains
	 * those {@link PreparedField} objects which are marked as relevant for
	 * asking.
	 *
	 * @param object Input object to inspected
	 * @return List of prepared fields which are marked as relevant for asking
	 */
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
