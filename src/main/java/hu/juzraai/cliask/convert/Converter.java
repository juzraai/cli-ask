package hu.juzraai.cliask.convert;

import hu.juzraai.cliask.core.PreparedField;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author Zsolt Jurányi
 */
public class Converter {

	private static final Map<Class<?>, ConvertTo<?>> CONVERTERS = new LinkedHashMap<>();

	static {
		// string (NOP)
		addConverter(String.class, new ConvertToString());

		// integers
		addConverter(Byte.TYPE, new ConvertToByte());
		addConverter(Short.TYPE, new ConvertToShort());
		addConverter(Integer.TYPE, new ConvertToInteger());
		addConverter(Long.TYPE, new ConvertToLong());

		// floats
		addConverter(Float.TYPE, new ConvertToFloat());
		addConverter(Double.TYPE, new ConvertToDouble());

		// boolean
		addConverter(Boolean.TYPE, new ConvertToBoolean());
	}

	public static <T> void addConverter(@Nonnull Class<T> type, @Nonnull ConvertTo<T> converter) {
		CONVERTERS.put(type, converter);
	}

	@Nonnull
	public static Object convert(@Nonnull String rawValue, @Nonnull PreparedField field) throws ConvertFailedException {
		Class<?> type = field.getField().getType();

		// TODO later: if converter class specified in field, use that one

		for (Map.Entry<Class<?>, ConvertTo<?>> entry : CONVERTERS.entrySet()) {
			if (type.isAssignableFrom(entry.getKey())) {
				return entry.getValue().convert(rawValue);
			}
		}

		throw new UnsupportedOperationException("No converter found for type: " + type.getName());
	}

}
