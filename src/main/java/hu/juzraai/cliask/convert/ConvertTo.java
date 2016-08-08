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

/**
 * Interface of converters which transform a raw input string into a target
 * type. The name of this interface was constructed so it can be read out
 * "Convert to X".
 *
 * @author Zsolt Jurányi
 */
public interface ConvertTo<T> {

	/**
	 * This method should convert the input value into the target type. If any
	 * error occurs, it should produce {@link ConvertFailedException} instead of
	 * other exceptions.
	 *
	 * @param rawValue The input value to be converted
	 * @return Result of the conversion
	 * @throws ConvertFailedException If any error occurs during conversion
	 */
	@Nonnull
	T convert(@Nonnull String rawValue) throws ConvertFailedException;
}
