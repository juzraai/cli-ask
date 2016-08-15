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
	 * Tries to find an appropriate converter in the internal storage for the
	 * given type.
	 * <p>
	 * First, it uses the map's <code>contains</code> and <code>get</code>
	 * method to fastly return the perfect converter, if any. If it fails,
	 * iterates through the entries and returns with the first converter which
	 * converts to a type assignable to the target type. So if the field
	 * type is <code>ClassA</code>, <code>ClassB</code> extends
	 * <code>ClassA</code>, and there's a  converter for <code>ClassB</code>
	 * it will be returned.
	 *
	 * @param type Type to search converter for
	 * @return An appropriate converter instance which converts from
	 * <code>String</code> to the given type, if any, or <code>null</code> if no
	 * appropriate converter found
	 */
	public static ConvertTo<?> selectConverter(Class<?> type) {

		// perfect converter
		if (CONVERTERS.containsKey(type)) {
			return CONVERTERS.get(type);
		}

		// converter for type's child class
		for (Map.Entry<Class<?>, ConvertTo<?>> entry : CONVERTERS.entrySet()) {
			if (type.isAssignableFrom(entry.getKey())) {
				return entry.getValue();
			}
		}

		// no converter found
		return null;
	}

}
