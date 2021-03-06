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

package hu.juzraai.cliask;

import hu.juzraai.cliask.annotation.Ask;
import hu.juzraai.cliask.convert.ConvertFailedException;
import hu.juzraai.cliask.convert.Converters;
import hu.juzraai.cliask.inspect.FieldInspector;
import hu.juzraai.cliask.inspect.ObjectInspector;
import hu.juzraai.cliask.inspect.PreparedField;
import hu.juzraai.cliask.util.AlignedText;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Scanner;

/**
 * This is the place of CLI-Ask's main functions, currently there are two main
 * ones:
 * <p>
 * You can ask for a string using the {@link #string(String, String)} method.
 * You must provide a label and you can specify a default value which will be
 * returned if user gives empty input. Otherwise user is forced to provide
 * non-empty input.
 * <p>
 * You can also ask for a POJO using {@link #object(String, Object)} method. You
 * provide an object which has some fields annotated with {@link Ask}, and
 * {@link AskFor} will ask the user for their values. The input string will be
 * converted automatically to the field type using {@link Converters}.
 *
 * @author Zsolt Jurányi
 */
public class AskFor { // TODO doc: about recursive

	// TODO README: about recursive

	protected static String generatePrintedLabel(String label, String defaultValue) {
		StringBuilder s = new StringBuilder();
		s.append(label);
		if (null != defaultValue) {
			s.append(String.format("%n%s", String.format("[default: '%s']", defaultValue)));
		}
		return String.format("%n%s", new AlignedText(40, AlignedText.Align.RIGHT, s.toString()).toString().replaceAll("\n$", " : "));
	}

	/**
	 * Requests user input for every field in the given <code>Object</code>
	 * argument which is annotated with {@link Ask}, and updates the object
	 * using values got from user.
	 * <p>
	 * If a field has a non-null value, that will be treated as a default value.
	 * If the user provides empty input for the field, it won't be modified.
	 * <p>
	 * If a field is <code>null</code>, user will be asked for its value until
	 * a non-empty input.
	 * <p>
	 * User's raw string input is converted into the type of the field using
	 * {@link Converters}. By default, it chooses the appropriate converter from
	 * the internal ones, but this can be overriden by specifying a converter
	 * class in {@link Ask} annotation.
	 * <p>
	 * If any error occurs during conversion, user will be asked again for a
	 * valid input value.
	 * <p>
	 * If no appropriate converter was found or something went bad when
	 * inspecting an input field, that field will be skipped from asking.
	 *
	 * @param label  If it's not <code>null</code>, this will be printed out
	 *               before asking for the field values, ":" will be appended to
	 *               its end
	 * @param object Object to be updated using user input
	 * @param <T>    Type of the object
	 * @return The object updated from user input
	 */
	@Nonnull
	public static <T> T object(String label, @Nonnull T object) { // TODO doc: about recursive

		// parse metadata
		List<PreparedField> fields = new ObjectInspector(new FieldInspector()).relevantFieldsOf(object);

		// if there's any field which needs to be asked
		if (!fields.isEmpty()) {

			// if we have a dataset name, write header
			if (null != label && !label.trim().isEmpty()) {
				System.out.printf("%n%s :%n", label);
			}

			// ask for fields
			for (PreparedField field : fields) {
				preparedField(field);
			}

		}

		return object;
	}

	/**
	 * Requests user input for every field in the given <code>Object</code>
	 * argument which is annotated with {@link Ask}, and updates the object
	 * using values got from user.
	 * <p>
	 * If a field has a non-null value, that will be treated as a default value.
	 * If the user provides empty input for the field, it won't be modified.
	 * <p>
	 * If a field is <code>null</code>, user will be asked for its value until
	 * a non-empty input.
	 * <p>
	 * User's raw string input is converted into the type of the field using
	 * {@link Converters}. By default, it chooses the appropriate converter from
	 * the internal ones, but this can be overriden by specifying a converter
	 * class in {@link Ask} annotation.
	 * <p>
	 * If any error occurs during conversion, user will be asked again for a
	 * valid input value.
	 * <p>
	 * If no appropriate converter was found or something went bad when
	 * inspecting an input field, that field will be skipped from asking.
	 * <p>
	 * This method simply calls {@link #object(String, Object)} with
	 * <code>null</code> as the <code>label</code> argument.
	 *
	 * @param object Object to be updated using user input
	 * @param <T>    Type of the object
	 * @return The object updated from user input
	 */
	@Nonnull
	public static <T> T object(@Nonnull T object) { // TODO doc: about recursive
		return object(null, object);
	}

	protected static void preparedField(@Nonnull PreparedField preparedField) {
		Object defaultValue = preparedField.getDefaultValue();
		String label = preparedField.getLabel();
		if (preparedField.isRecursive()) { // avoiding of infinite loops handled in field inspector
			object(label, defaultValue);
		} else {
			String dv = null == defaultValue ? null : defaultValue.toString();
			boolean repeat;
			do {
				repeat = false;
				String rawValue = string(label, dv);
				try {
					if (!rawValue.equals(dv)) { // no need to process default value

						// TODO later: use raw value validator specified on field

						Object value = preparedField.getConverter().convert(rawValue);

						// TODO later: use value validator specified on field

						preparedField.set(value);
					}
				} catch (Exception e) {
					repeat = e instanceof ConvertFailedException; // TODO later: or validation exceptions
					System.out.printf("%40s   %s%n", "", e.getMessage());
				}
			} while (repeat);
		}
	}

	/**
	 * Requests user input. Prints out a label in front of the input cursor.
	 * If user only hits ENTER (or input string is empty after trimming) AND
	 * default value is <code>null</code>, asks the user again until a non-empty
	 * input. If the input is empty and there's a default value, returns the
	 * default value.
	 *
	 * @param label        Label to be printed out in front of input cursor, ":"
	 *                     will be appended to its end
	 * @param defaultValue Default value to be used if user provides empty
	 *                     input. If default value is <code>null</code>, user
	 *                     will be asked again until a non-empty input.
	 * @return The user's non-empty input, or the default value if it's not
	 * <code>null</code>
	 */
	@Nonnull
	public static String string(@Nonnull String label, String defaultValue) {
		// TODO: acceptEmpty option?

		// build up output

		String printedLabel = generatePrintedLabel(label, defaultValue);

		// ask for value

		Scanner input = new Scanner(System.in);
		String value;
		boolean repeat;
		do {
			repeat = false;
			System.out.print(printedLabel);
			value = input.nextLine().trim();
			if (value.isEmpty() && null == defaultValue) {
				repeat = true;
				System.out.printf("%40s   There's no default value, please try again!%n", "");
			}
		} while (repeat);

		return value.isEmpty() ? defaultValue : value;
	}

	/**
	 * Requests user input. Prints out a label in front of the input cursor.
	 * If user only hits ENTER (or input string is empty after trimming), asks
	 * the user again until a non-empty input.
	 * <p>
	 * This method simply calls {@link #string(String, String)} with
	 * <code>null</code> as the default value argument.
	 *
	 * @param label Label to be printed out in front of input cursor, ":" will
	 *              be appended to its end
	 * @return The user's non-empty input
	 */
	@Nonnull
	public static String string(@Nonnull String label) {
		return string(label, null);
	}
}
