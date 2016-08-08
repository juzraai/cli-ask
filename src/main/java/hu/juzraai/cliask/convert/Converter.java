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

package hu.juzraai.cliask.convert;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Converter engine used in CLI-Ask, to convert String input into various types.
 * It holds a Map of converters where key is the target type and value is the
 * appropriate converter instance.
 * <p>
 * Its static initializer adds default converters into the map for String (NOP)
 * and all primitives and their boxed types.
 * <p>
 * Of course, default converters can be overriden and new (type,converter) pairs
 * can be added.
 *
 * @author Zsolt Jurányi
 */
public class Converter {

	private static final Map<Class<?>, ConvertTo<?>> CONVERTERS = new LinkedHashMap<>();

	static {
		// string (NOP)
		addConverter(String.class, new ConvertToString());

		// integers
		addConverter(Byte.TYPE, new ConvertToByte());
		addConverter(Byte.class, new ConvertToByte());
		addConverter(Short.TYPE, new ConvertToShort());
		addConverter(Short.class, new ConvertToShort());
		addConverter(Integer.TYPE, new ConvertToInteger());
		addConverter(Integer.class, new ConvertToInteger());
		addConverter(Long.TYPE, new ConvertToLong());
		addConverter(Long.class, new ConvertToLong());

		// floats
		addConverter(Float.TYPE, new ConvertToFloat());
		addConverter(Float.class, new ConvertToFloat());
		addConverter(Double.TYPE, new ConvertToDouble());
		addConverter(Double.class, new ConvertToDouble());

		// boolean
		addConverter(Boolean.TYPE, new ConvertToBoolean());
		addConverter(Boolean.class, new ConvertToBoolean());
	}

	/**
	 * Adds (or replaces) converter for the given type.
	 *
	 * @param type      Target type of the converter
	 * @param converter Converter instance
	 * @param <T>       Target type of the converter
	 */
	public static <T> void addConverter(@Nonnull Class<T> type, @Nonnull ConvertTo<T> converter) {
		CONVERTERS.put(type, converter);
	}

	/**
	 * Converts the given string into the given target type. If third argument
	 * is non-null, uses that as converter, otherwise tries to find converter
	 * from the internal storage.
	 * <p>
	 * If no proper converter was found, throws {@link UnsupportedOperationException}.
	 *
	 * @param rawValue  Input string value to be converted
	 * @param type      Target type
	 * @param converter Optional converter to be used instead of internal
	 *                  converters
	 * @return The result of the conversion
	 * @throws ConvertFailedException If conversion fails
	 */
	@Nonnull
	public static Object convert(@Nonnull String rawValue, @Nonnull Class<?> type, ConvertTo<?> converter) throws ConvertFailedException {
		if (null != converter) {
			return converter.convert(rawValue);
		} else {
			for (Map.Entry<Class<?>, ConvertTo<?>> entry : CONVERTERS.entrySet()) {
				if (type.isAssignableFrom(entry.getKey())) {
					return entry.getValue().convert(rawValue);
				}
			}
		}

		throw new UnsupportedOperationException("No converter found for type: " + type.getName());
	}

}
