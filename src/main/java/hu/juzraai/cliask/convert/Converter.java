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

		if (null != field.getConverter()) {
			return field.getConverter().convert(rawValue);
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
