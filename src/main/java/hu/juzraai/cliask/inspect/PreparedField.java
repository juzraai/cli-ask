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
import hu.juzraai.toolbox.log.LoggerFactory;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

/**
 * Prepares (creates interpreted version of) an object's field by deciding
 * whether it's relevant for asking, determining label, default value, and
 * providing converter instance.
 *
 * @author Zsolt Jurányi
 */
public class PreparedField { // TODO doc

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

	/**
	 * @return The {@link Ask} annotation of the field or <code>null</code> if
	 * it hasn't got one
	 */
	public Ask getAsk() {
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
	 * @return The default value of the field
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

	/**
	 * @return Whether the field should be handled in recursive mode
	 */
	public boolean isRecursive() {
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
