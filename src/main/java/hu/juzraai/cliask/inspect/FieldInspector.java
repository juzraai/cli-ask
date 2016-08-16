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
 * Inspects a given field by extracting it's type, value and arguments of it's
 * {@link Ask} annotation. Generates a label for the field and decides whether
 * the field is relevant for asking, whether it should be handled
 * recursively, and selects an appropriate converter for it.
 *
 * @author Zsolt Jurányi
 */
public class FieldInspector { // TODO doc

	private static final Logger L = LoggerFactory.getLogger(FieldInspector.class);

	/**
	 * Determines whether the given field is relevant for asking, sets it's
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
	 * @return
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
	 * @throws InspectFailedException
	 */
	protected void extractDefaultValue(@Nonnull PreparedField preparedField) throws InspectFailedException {
		try {
			preparedField.getField().setAccessible(true); // throws SE
			preparedField.setDefaultValue(preparedField.getField().get(preparedField.getObject())); // throws IAE
		} catch (Exception e) {
			throw new InspectFailedException("Failed to get field value", e);
		}
	}

	/**
	 * Generates the label for the given field. If {@link Ask} has a non-empty
	 * label in it's <code>value</code> argument, that value will be used as
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
		Ask ask = preparedField.getAsk();
		String defaultLabel = useFieldNameAsDefault ? preparedField.getField().getName() : null;
		preparedField.setLabel(ask.value().trim().isEmpty() ? defaultLabel : ask.value());
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

	protected void instantiateDefaultValue(@Nonnull PreparedField preparedField) throws InspectFailedException {
		Field field = preparedField.getField();
		try {
			preparedField.setDefaultValue(field.getType().newInstance()); // throws IE, IAE
			field.set(preparedField.getObject(), preparedField.getDefaultValue()); // throws IAE
		} catch (Exception e) {
			throw new InspectFailedException("Failed to instantiate field value, probably missing no-arg constructor", e);
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

}
