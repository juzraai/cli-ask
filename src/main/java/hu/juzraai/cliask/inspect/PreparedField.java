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

import hu.juzraai.cliask.annotation.Ask;
import hu.juzraai.cliask.convert.ConvertTo;
import hu.juzraai.cliask.convert.Converters;
import hu.juzraai.cliask.convert.DefaultConverter;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.NoSuchAlgorithmException;

/**
 * Prepares (creates interpreted version of) an object's field by deciding
 * whether it's relevant for asking, determining label, default value, and
 * providing converter instance.
 *
 * @author Zsolt Jurányi
 */
public class PreparedField {

	private static final Logger L = LoggerFactory.getLogger(PreparedField.class);

	private final Object object;
	private final Field field;
	private final Ask ask;
	private boolean recursive;
	private boolean relevant;
	private String label;
	private Object defaultValue;
	private ConvertTo<?> converter;

	public PreparedField(@Nonnull Object object, @Nonnull Field field, Ask ask) {
		this.object = object;
		this.field = field;
		this.ask = ask;
	}

	@Deprecated
	private static void determineRelevance(@Nonnull PreparedField preparedField) {
		preparedField.relevant = null != preparedField.ask && ((preparedField.field.getModifiers() & Modifier.FINAL) != Modifier.FINAL);
	}

	@Deprecated
	private static void extractDefaultValue(@Nonnull PreparedField preparedField) throws IllegalAccessException {
		try {
			preparedField.field.setAccessible(true); // throws SE
			preparedField.defaultValue = preparedField.field.get(preparedField.object); // throws IAE
		} catch (Exception e) {
			IllegalAccessException t = new IllegalAccessException("Failed to get value of field");
			t.initCause(e);
			throw t;
		}
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
	 * object, otherwise an appropriate converter will be selected using
	 * {@link Converters} class.
	 * <p>
	 * If no converter found or any error occurs during field inspection, the
	 * field will be irrelevant and skipped from asking.
	 *
	 * @param field  Field to be prepared
	 * @param object Parent object (needed for get/set operations)
	 * @return A {@link PreparedField} object containing the field, the parent
	 * object, the label, the default value and the converter instance
	 */
	@Deprecated
	@Nonnull
	public static PreparedField prepare(@Nonnull Field field, @Nonnull Object object) { // TODO doc: about recursive
		Ask ask = field.getAnnotation(Ask.class);
		PreparedField preparedField = new PreparedField(object, field, ask);
		try {
			determineRelevance(preparedField);
			if (preparedField.relevant) {
				extractDefaultValue(preparedField); // throws IAE
				if (ask.recursive()) {
					preparedField.recursive = true;
					prepareRecursiveField(preparedField); // throws IAE, IE
				} else {
					prepareNonRecursiveField(preparedField); // throws IAE, IE, NSAE
				}
			}
		} catch (Exception e) {
			L.warn("Error occurred while preparing field '{}' of class '{}': {}", field.getName(), object.getClass().getName(), e.getMessage());
			L.trace("Stack trace", e);
			preparedField.relevant = false; // field will be skipped silently
		}
		return preparedField;
	}

	@Deprecated
	private static void prepareNonRecursiveField(@Nonnull PreparedField preparedField) throws IllegalAccessException, NoSuchAlgorithmException, InstantiationException {
		Ask ask = preparedField.ask;

		// get converter
		preparedField.converter = provideConverter(preparedField, ask); // throws IAE, IE, NSAE

		// generate label
		preparedField.label = ask.value().trim().isEmpty() ? preparedField.field.getName() : ask.value();
	}

	@Deprecated
	private static void prepareRecursiveField(@Nonnull PreparedField preparedField) throws InstantiationException, IllegalArgumentException {

		// try to instantiate default value
		if (null == preparedField.defaultValue) {
			Field field = preparedField.field;
			try {
				preparedField.defaultValue = field.getType().newInstance(); // throws IE, IAE
				field.set(preparedField.object, preparedField.defaultValue); // throws IAE
			} catch (Exception e) {
				InstantiationException t = new InstantiationException("Failed to instantiate value for field, probably no-arg constructor is missing in type: " + field.getType().getName());
				t.initCause(e);
				throw t;
			}
		}

		// avoid infinite loops
		if (preparedField.object.getClass().isAssignableFrom(preparedField.defaultValue.getClass())) {
			throw new IllegalArgumentException("Value of recursive field has type same as or inherited type of the parent object - it would lead to recursion!");
		}

		// generate label
		Ask ask = preparedField.ask;
		preparedField.label = ask.value().trim().isEmpty() ? null : ask.value();
	}

	@Deprecated
	@Nonnull
	private static ConvertTo<?> provideConverter(@Nonnull PreparedField preparedField, @Nonnull Ask ask) throws IllegalAccessException, InstantiationException, NoSuchAlgorithmException {
		ConvertTo<?> converter = DefaultConverter.class.equals(ask.converter())
				? Converters.find(preparedField.field.getType())
				: ask.converter().newInstance();
		if (null == converter) {
			throw new NoSuchAlgorithmException("No converter found for type: " + preparedField.field.getType().getName());
		}
		// TODO verify custom converter class' generic type
		return converter;
	}

	public Ask getAsk() { // TODO doc
		return ask;
	}

	/**
	 * @return Instance of the converter class specified as {@link Ask}
	 * annotation's attribute, or <code>null</code> if no class were specified
	 * or instantiation failed
	 */
	public ConvertTo<?> getConverter() {
		return converter;
	}

	public void setConverter(ConvertTo<?> converter) {
		this.converter = converter;
	}

	/**
	 * @return The default value of the field - the value of the field when
	 * {@link #prepare(Field, Object)} was called
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
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

	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return The parent object
	 */
	@Nonnull
	public Object getObject() {
		return object;
	}

	public boolean isRecursive() { // TODO doc
		return recursive;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	/**
	 * @return Whether this field should be asked from user
	 */
	public boolean isRelevant() {
		return relevant;
	}

	public void setRelevant(boolean relevant) {
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
