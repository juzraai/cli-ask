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

import hu.juzraai.cliask.convert.ConvertFailedException;
import hu.juzraai.cliask.convert.Converter;

import javax.annotation.Nonnull;
import java.util.Scanner;

/**
 * This is the place of CLI-Ask's main functions.
 *
 * @author Zsolt Jurányi
 */
public class AskFor { // TODO doc

	@Nonnull
	public static <T> T object(@Nonnull T object) {
		return object(null, object);
	}

	@Nonnull
	public static <T> T object(String label, @Nonnull T object) {

		// parse metadata
		PreparedObject preparedObject = PreparedObject.prepare(object);

		// if there's any field which needs to be asked
		if (!preparedObject.getFields().isEmpty()) {

			// if we have a dataset name, write header
			if (null != label && !label.trim().isEmpty()) {
				System.out.printf("%n%s :%n", label);
			}

			// ask for fields
			for (PreparedField field : preparedObject.getFields()) {
				preparedField(field);
			}

		}

		return object;
	}

	protected static void preparedField(@Nonnull PreparedField field) {
		String dv = null == field.getDefaultValue() ? null : field.getDefaultValue().toString();
		boolean repeat;
		Object value = null;
		do {
			repeat = false;
			String rawValue = string(field.getLabel(), dv);
			try {
				if (!rawValue.equals(dv)) { // no need to process default value

					// TODO later: use raw value validator specified on field

					value = Converter.convert(rawValue, field.getField().getType(), field.getConverter());

					// TODO later: use value validator specified on field

					field.set(value);
				}
			} catch (Exception e) {
				repeat = e instanceof ConvertFailedException; // TODO later: or validation exceptions
				System.out.printf("%40s   %s%n", "", e.getMessage());
			}
		} while (repeat);
	}

	@Nonnull
	public static String string(@Nonnull String label) {
		return string(label, null);
	}

	@Nonnull
	public static String string(@Nonnull String name, String defaultValue) {
		// TODO: acceptEmpty option?

		// build up output

		StringBuilder s = new StringBuilder();
		s.append(String.format("%n%40s", name));
		if (null != defaultValue) {
			s.append(String.format("%n%40s", String.format("[default: '%s']", defaultValue)));
		}
		s.append(" : ");

		// ask for value

		Scanner input = new Scanner(System.in);
		String value;
		boolean repeat;
		do {
			repeat = false;
			System.out.print(s.toString());
			value = input.nextLine().trim();
			if (value.isEmpty() && null == defaultValue) {
				repeat = true;
				System.out.printf("%40s   There's no default value, please try again!%n", "");
			}
		} while (repeat);

		return value.isEmpty() ? defaultValue : value;
	}
}
