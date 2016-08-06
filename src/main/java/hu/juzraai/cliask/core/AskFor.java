package hu.juzraai.cliask.core;

import hu.juzraai.cliask.convert.ConvertFailedException;
import hu.juzraai.cliask.convert.Converter;

import javax.annotation.Nonnull;
import java.util.Scanner;

/**
 * @author Zsolt Jurányi
 */
public class AskFor {

	@Nonnull
	public static <T> T object(@Nonnull T object) {

		// parse metadata
		PreparedObject preparedObject = PreparedObject.prepare(object);

		// if there's any field which needs to be asked
		if (!preparedObject.getFields().isEmpty()) {

			// if we have a dataset name, write header
			if (!preparedObject.getName().isEmpty()) {
				System.out.printf("%nPlease provide %s :%n", preparedObject.getName());
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
			String rawValue = string(field.getName(), dv);

			try {
				// TODO later: use raw value validator specified on field

				value = Converter.convert(rawValue, field);

				// TODO later: use value validator specified on field

				field.set(value);
			} catch (Exception e) {
				repeat = e instanceof ConvertFailedException; // TODO later: or validation exceptions
				System.out.printf("%40s   %s%n", "", e.getMessage());
			}
		} while (repeat);

	}

	@Nonnull
	public static String string(@Nonnull String name, String defaultValue) {

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
