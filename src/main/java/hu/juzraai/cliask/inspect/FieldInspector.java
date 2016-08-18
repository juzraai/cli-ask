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
 * Inspects a given field by extracting its type, value and arguments of its
 * {@link Ask} annotation. Generates a label for the field and decides whether
 * the field is relevant for asking, whether it should be handled
 * recursively, and selects an appropriate converter for it.
 *
 * @author Zsolt Jurányi
 */
public class FieldInspector {

	private static final Logger L = LoggerFactory.getLogger(FieldInspector.class);

	/**
	 * Determines whether the given field is relevant for asking, sets its
	 * <code>relevant</code> field and returns with the decision too.
	 * <p>
	 * A field is relevant for asking if it has {@link Ask} annotation and if
	 * it's not <code>final</code>.
	 * <p>
	 * Please note that this field is used to decide about relevance in an early
	 * phase, it only uses the field's metadata. Other methods may mark the
	 * field irrelevant if errors occur during inspection.
	 *
	 * @param preparedField Prepared field to be inspected
	 * @return Whether the field is relevant for asking - based on its metadata
	 */
	protected boolean determineIfRelevant(@Nonnull PreparedField preparedField) {
		boolean r = null != preparedField.getAsk() && ((preparedField.getField().getModifiers() & Modifier.FINAL) != Modifier.FINAL);
		preparedField.setRelevant(r);
		return r;
	}

	/**
	 * Calls <code>get(Object)</code> method of {@link Field} and sets the
	 * returned value as the default value of the prepared field.
	 * <p>
	 * If field is not accessible or any error occurs when getting the value,
	 * throws {@link InspectFailedException}.
	 *
	 * @param preparedField Prepared field to be inspected
	 * @throws InspectFailedException if any error occurred when getting default
	 *                                value
	 */
	protected void extractDefaultValue(@Nonnull PreparedField preparedField) throws InspectFailedException {
		try {
			Field field = preparedField.getField();
			field.setAccessible(true); // throws SE
			preparedField.setDefaultValue(field.get(preparedField.getObject())); // throws IAE
		} catch (Exception e) {
			throw new InspectFailedException("Failed to get field value", e);
		}
	}

	/**
	 * Generates the label for the given field. If {@link Ask} has a non-empty
	 * label in its <code>value</code> argument, that value will be used as
	 * label. Otherwise the field name or <code>null</code> will be used
	 * according to the value of <code>useFieldNameAsDefault</code> argument.
	 * <p>
	 * Default <code>null</code> label is used in recursive mode, because that
	 * label is used for the "sub-POJO" and POJO labels are designed to be
	 * optional.
	 *
	 * @param preparedField         Prepared field to be inspected
	 * @param useFieldNameAsDefault Whether to use the field name or
	 *                              <code>null</code> as label when {@link
	 *                              Ask}'s <code>value</code> is empty
	 */
	protected void generateLabel(@Nonnull PreparedField preparedField, boolean useFieldNameAsDefault) {
		String label = preparedField.getAsk().value();
		String defaultLabel = useFieldNameAsDefault ? preparedField.getField().getName() : null;
		preparedField.setLabel(label.trim().isEmpty() ? defaultLabel : label);
	}

	/**
	 * Marks the given prepared field to irrelevant then logs the given
	 * exception.
	 *
	 * @param preparedField Prepared field which couldn't be inspected
	 *                      completely
	 * @param e             Exception to be logged
	 */
	protected void handleInspectionFail(@Nonnull PreparedField preparedField, @Nonnull InspectFailedException e) {
		preparedField.setRelevant(false);
		L.warn(String.format("Error while inspecting field '%s.%s' - %s",
				preparedField.getObject().getClass().getName(),
				preparedField.getField().getName(),
				e.getMessage()),
				e);
	}

	/**
	 * Constructs a {@link PreparedField} object with the initially known
	 * values: the input field, its parent object and the {@link Ask}
	 * annotation.
	 * <p>
	 * It also sets <code>recursive</code> property of the prepared field based
	 * on the annotations's appropriate argument.
	 *
	 * @param field  Input field to be inspected
	 * @param object Parent object of the input field
	 * @return Prepared field with the following values: input field, parent
	 * object, {@link Ask} annotation and <code>recursive</code>
	 */
	protected PreparedField initializeField(@Nonnull Field field, @Nonnull Object object) {
		Ask ask = field.getAnnotation(Ask.class);
		PreparedField preparedField = new PreparedField(object, field, ask);
		preparedField.setRecursive(ask.recursive());
		return preparedField;
	}

	/**
	 * Inspects the given field of the given object and returns with a {@link
	 * PreparedField} which contains prepared values to be used when asking.
	 * <p>
	 * At first, it constructs the initial {@link PreparedField} object using
	 * {@link #initializeField(Field, Object)} method. Then it calls {@link
	 * #determineIfRelevant(PreparedField)} and if it returns with true, uses
	 * {@link #inspectRelevantField(PreparedField)} to do the deeper
	 * inspections (default value, converter, recursive field's verifications).
	 *
	 * @param field  Input field to be inspected
	 * @param object Parent object of the input field
	 * @return Prepared field which contains the input field, the parent object,
	 * the {@link Ask} annotation, whether it's relevant, whether it's
	 * recursive, the default value, the converter and the label
	 */
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

	/**
	 * Inspects a field which is marked relevant and non-recursive. First, it
	 * selects/constructs converter instance using {@link
	 * #provideConverter(PreparedField)} method, then generates a label using
	 * {@link #generateLabel(PreparedField, boolean)}.
	 * <p>
	 * If any error eccurs during converter selection/construction, throws an
	 * {@link InspectFailedException}.
	 * <p>
	 * The default label will be the field name, if {@link Ask} doesn't specify
	 * a custom label.
	 *
	 * @param preparedField Prepared field to be inspected
	 * @throws InspectFailedException if any error occured during converter
	 *                                construction, selection, or setting
	 */
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

	/**
	 * Inspects a field which is marked relevant and recursive. If default value
	 * is <code>null</code>, tries to instantiate it first by calling {@link
	 * #instantiateDefaultValue(PreparedField)}. Then it checks whether parent
	 * object's type is assignable from the field type - if so, that would lead
	 * to an infinite loop, so throws an {@link InspectFailedException} to avoid
	 * it. Finally generates the label using {@link #generateLabel(PreparedField,
	 * boolean)}, the default label will be <code>null</code>, if {@link Ask}
	 * doesn't specify a custom label.
	 *
	 * @param preparedField Prepared field to be inspected
	 * @throws InspectFailedException if any error occurred during default value
	 *                                instantiation and setting, or if recursion
	 *                                would lead to infinite loop
	 */
	protected void inspectRecursiveField(@Nonnull PreparedField preparedField) throws InspectFailedException {

		// try to instantiate default value
		if (null == preparedField.getDefaultValue()) {
			instantiateDefaultValue(preparedField);
		}

		// avoid infinite loops
		if (preparedField.getObject().getClass().isAssignableFrom(preparedField.getDefaultValue().getClass())) {
			throw new InspectFailedException("Parent object's type is assignable from type of recursive field's value - this would lead to infinite loop!");
		}

		// generate label - null if not specified
		generateLabel(preparedField, false);
	}

	/**
	 * Inspects a field which is marked relevant before. Calls {@link
	 * #extractDefaultValue(PreparedField)} first, then {@link
	 * #inspectNonRecursiveField(PreparedField)} or {@link
	 * #inspectNonRecursiveField(PreparedField)} based on <code>recursive</code>
	 * property of the given prepared field.
	 * <p>
	 * Doesn't catch {@link InspectFailedException}'s throwed by called methods.
	 *
	 * @param preparedField Prepared field to be inspected
	 * @throws InspectFailedException if any error occurred during inspection
	 */
	protected void inspectRelevantField(@Nonnull PreparedField preparedField) throws InspectFailedException {
		extractDefaultValue(preparedField);
		if (preparedField.isRecursive()) {
			inspectRecursiveField(preparedField);
		} else {
			inspectNonRecursiveField(preparedField);
		}
	}

	/**
	 * Instantiates the default value by calling the no-arg constructor of the
	 * field's type. After constructing the object, set's this value to the
	 * field. If any error occurs during this operations, throws an {@link
	 * InspectFailedException}.
	 * <p>
	 * This instantiation is needed in recursive mode when default value is
	 * null.
	 *
	 * @param preparedField Prepared field to be inspected
	 * @throws InspectFailedException if any error occurred during instantiation
	 *                                and setting
	 */
	protected void instantiateDefaultValue(@Nonnull PreparedField preparedField) throws InspectFailedException {
		Field field = preparedField.getField();
		try {
			preparedField.setDefaultValue(field.getType().newInstance()); // throws IE, IAE
			field.set(preparedField.getObject(), preparedField.getDefaultValue()); // throws IAE
		} catch (Exception e) {
			throw new InspectFailedException("Failed to instantiate field value, probably missing no-arg constructor", e);
		}
	}

	/**
	 * Instantiates the custom converter if the class was specified in {@link
	 * Ask}, or selects a converter from the pool using {@link Converters}.
	 * Exceptions thrown by Reflection when constructing the objects won't be
	 * catched and if no suitable constructor available, throws {@link
	 * NoSuchAlgorithmException}.
	 *
	 * @param preparedField Prepared field to be inspected
	 * @return A converter instance - of the custom converter class or the
	 * selected converter from the pool, if any
	 * @throws IllegalAccessException   if the class or its nullary constructor
	 *                                  is not accessible
	 * @throws InstantiationException   if converter class represents an
	 *                                  abstract class, an interface, an array
	 *                                  class, a primitive type, or void; or if
	 *                                  the class has no nullary constructor; or
	 *                                  if the instantiation fails for some
	 *                                  other reason
	 * @throws NoSuchAlgorithmException if no suitable converter found in the
	 *                                  pool for the field type
	 */
	@Nonnull
	protected ConvertTo<?> provideConverter(@Nonnull PreparedField preparedField) throws IllegalAccessException, InstantiationException, NoSuchAlgorithmException {
		Class<? extends ConvertTo<?>> converterClass = preparedField.getAsk().converter();
		Class<?> fieldType = preparedField.getField().getType();
		ConvertTo<?> converter = DefaultConverter.class.equals(converterClass)
				? Converters.find(fieldType)
				: converterClass.newInstance();
		if (null == converter) {
			throw new NoSuchAlgorithmException("No converter found for type: " + fieldType.getName());
		}
		// TODO verify custom converter class' generic type
		return converter;
	}

}
