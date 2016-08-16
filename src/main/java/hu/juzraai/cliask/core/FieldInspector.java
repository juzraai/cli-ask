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
import hu.juzraai.cliask.convert.Converters;
import hu.juzraai.cliask.convert.DefaultConverter;
import hu.juzraai.toolbox.log.LoggerFactory;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.NoSuchAlgorithmException;

/**
 * @author Zsolt Jurányi
 */
public class FieldInspector { // TODO doc

	private static final Logger L = LoggerFactory.getLogger(FieldInspector.class);

	protected boolean determineIfRelevant(@Nonnull PreparedField preparedField) {
		boolean r = null != preparedField.getAsk() && ((preparedField.getField().getModifiers() & Modifier.FINAL) != Modifier.FINAL);
		preparedField.setRelevant(r);
		return r;
	}

	protected void extractDefaultValue(@Nonnull PreparedField preparedField) throws InspectFailedException {
		try {
			preparedField.getField().setAccessible(true); // throws SE
			preparedField.setDefaultValue(preparedField.getField().get(preparedField.getObject())); // throws IAE
		} catch (Exception e) {
			throw new InspectFailedException("Failed to get field value", e);
		}
	}

	protected void handleInspectionFail(@Nonnull PreparedField preparedField, @Nonnull InspectFailedException e) {
		preparedField.setRelevant(false);
		L.warn(String.format("Error while inspecting field '%s.%s' - %s",
				preparedField.getObject().getClass().getName(),
				preparedField.getField().getName(),
				e.getMessage()),
				e);
	}

	protected PreparedField initializeField(@Nonnull Field field, @Nonnull Object object) {
		Ask ask = field.getAnnotation(Ask.class);
		PreparedField preparedField = new PreparedField(object, field, ask);
		preparedField.setRecursive(ask.recursive());
		return preparedField;
	}

	@Nonnull
	public PreparedField inspectField(@Nonnull Field field, @Nonnull Object object) {
		PreparedField preparedField = initializeField(field, object);
		if (determineIfRelevant(preparedField)) {
			try {
				inspectRelevantField(preparedField);
			} catch (InspectFailedException e) {
				handleInspectionFail(preparedField, e);
			}
		}
		return preparedField;
	}

	protected void inspectNonRecursiveField(@Nonnull PreparedField preparedField) throws InspectFailedException {

		// get converter
		try {
			preparedField.setConverter(provideConverter(preparedField)); // throws IAE, IE, NSAE
		} catch (Exception e) {
			throw new InspectFailedException("Failed to select/construct converter", e);
		}

		// generate label - field name if not specified
		generateLabel(preparedField, true);
	}

	protected void instantiateDefaultValue(@Nonnull PreparedField preparedField) throws InspectFailedException {
		Field field = preparedField.getField();
		try {
			preparedField.setDefaultValue(field.getType().newInstance()); // throws IE, IAE
			field.set(preparedField.getObject(), preparedField.getDefaultValue()); // throws IAE
		} catch (Exception e) {
			throw new InspectFailedException("Failed to instantiate field value, probably missing no-arg constructor", e);
		}
	}

	protected void inspectRecursiveField(@Nonnull PreparedField preparedField) throws InspectFailedException {

		// try to instantiate default value
		if (null == preparedField.getDefaultValue()) {
			instantiateDefaultValue(preparedField);
		}

		// avoid infinite loops
		if (preparedField.getObject().getClass().isAssignableFrom(preparedField.getDefaultValue().getClass())) {
			throw new InspectFailedException("Parent object's type is assignable from type of recursive field's value - this would lead to recursion!");
		}

		// generate label - null if not specified
		generateLabel(preparedField, false);
	}

	protected void inspectRelevantField(@Nonnull PreparedField preparedField) throws InspectFailedException {
		extractDefaultValue(preparedField);
		if (preparedField.isRecursive()) {
			inspectRecursiveField(preparedField);
		} else {
			inspectNonRecursiveField(preparedField);
		}
	}

	protected ConvertTo<?> provideConverter(@Nonnull PreparedField preparedField) throws IllegalAccessException, InstantiationException, NoSuchAlgorithmException {
		ConvertTo<?> converter = DefaultConverter.class.equals(preparedField.getAsk().converter())
				? Converters.find(preparedField.getField().getType())
				: preparedField.getAsk().converter().newInstance();
		if (null == converter) {
			throw new NoSuchAlgorithmException("No converter found for type: " + preparedField.getField().getType().getName());
		}
		// TODO verify custom converter class' generic type
		return converter;
	}

	protected void generateLabel(@Nonnull PreparedField preparedField, boolean useFieldNameAsDefault) {
		Ask ask = preparedField.getAsk();
		String defaultLabel = useFieldNameAsDefault ? preparedField.getField().getName() : null;
		preparedField.setLabel(ask.value().trim().isEmpty() ? defaultLabel : ask.value());
	}

}
