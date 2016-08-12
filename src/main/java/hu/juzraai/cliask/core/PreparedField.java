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
import hu.juzraai.cliask.convert.ConvertTo;
import hu.juzraai.cliask.convert.DefaultConverter;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Prepares (creates interpreted version of) an object's field by deciding
 * whether it's relevant for asking, determining label and default value,
 * constructing explicitly specified converter if any.
 *
 * @author Zsolt Jurányi
 */
public class PreparedField {

	private static final Logger L = LoggerFactory.getLogger(PreparedField.class);

	private final Object object;
	private final Field field;
	private boolean relevant;
	private String label;
	private Object defaultValue;
	private ConvertTo<?> converter;

	public PreparedField(Object object, Field field) {
		this.object = object;
		this.field = field;
	}

	protected static ConvertTo<?> instantiateConverter(Ask ask) throws IllegalAccessException, InstantiationException {
		Class<? extends ConvertTo<?>> converterClass = ask.converter();
		return DefaultConverter.class.equals(converterClass) ? null : converterClass.newInstance();
	}

	/**
	 * Prepares (creates interpreted version of) an object's field by
	 * determining some values and returning them in a {@link PreparedField}
	 * object.
	 * <p>
	 * Decides whether the field is relevant: a field is relevant (will be
	 * asked) if it has {@link Ask} annotation, if it's non-final and if it's
	 * accessible ({@link Field}.setAccessible()).
	 * <p>
	 * Determines the label to be used for this field when asking: by default,
	 * the label is the field name, but <code>value</code> of {@link Ask}
	 * annotation can override it.
	 * <p>
	 * Determines default value by reading the current value of the field in the
	 * object.
	 * <p>
	 * Finally, if a converter class is specified in {@link Ask} annotation, it
	 * will be instantiated here and stored in the result {@link PreparedField}
	 * object.
	 *
	 * @param field  Field to be prepared
	 * @param object Parent object (needed for get/set operations)
	 * @return A {@link PreparedField} object containing the field, the parent
	 * object, the label, the default value and the converter instance
	 */
	@Nonnull
	public static PreparedField prepare(@Nonnull Field field, @Nonnull Object object) {
		PreparedField preparedField = new PreparedField(object, field);
		try {
			field.setAccessible(true); // throws SE

			Ask ask = field.getAnnotation(Ask.class);

			preparedField.setRelevant(null != ask && ((field.getModifiers() & Modifier.FINAL) != Modifier.FINAL));

			// TODO later: maybe we can check if there's proper converter or @AskRecursively

			if (preparedField.isRelevant()) {
				preparedField.setConverter(instantiateConverter(ask)); // throws IE, IAE
				preparedField.setDefaultValue(field.get(object)); // throws IAE
				preparedField.setLabel(ask.value().trim().isEmpty() ? field.getName() : ask.value());
			}
		} catch (SecurityException | InstantiationException | IllegalAccessException e) {
			L.warn("Error occurred while preparing field '{}' of class '{}': {}", field.getName(), object.getClass().getName(), e.getMessage());
			L.trace("Stack trace", e);
			preparedField.setRelevant(false);
		}
		return preparedField;
	}

	/**
	 * @return Instance of the converter class specified as {@link Ask}
	 * annotation's attribute, or <code>null</code> if no class were specified
	 * or instantiation failed
	 */
	public ConvertTo<?> getConverter() {
		return converter;
	}

	protected void setConverter(ConvertTo<?> converter) {
		this.converter = converter;
	}

	/**
	 * @return The default value of the field - the value of the field when
	 * {@link #prepare(Field, Object)} was called
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	protected void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return The field
	 */
	@Nonnull
	public Field getField() {
		return field;
	}

	/**
	 * @return The <code>value</code> attribute of {@link Ask} annotation, or
	 * the field name
	 */
	@Nonnull
	public String getLabel() {
		return label;
	}

	protected void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return The parent object
	 */
	@Nonnull
	public Object getObject() {
		return object;
	}

	/**
	 * @return Whether this field should be asked from user
	 */
	public boolean isRelevant() {
		return relevant;
	}

	protected void setRelevant(boolean relevant) {
		this.relevant = relevant;
	}

	/**
	 * Sets the given value for this field of the parent object, but swallows
	 * the exceptions.
	 *
	 * @param value New value to be set for this field of the parent object
	 */
	public void set(Object value) {
		if (relevant) {
			try {
				field.set(object, value);
			} catch (IllegalAccessException e) {
				L.warn("Error occurred while setting value for field '{}' of class '{}': {}", field.getName(), object.getClass().getName(), e.getMessage());
				L.trace("Stack trace", e);
			}
		} else {
			throw new IllegalStateException("set() called on irrelevant PreparedField");
		}
	}

}
