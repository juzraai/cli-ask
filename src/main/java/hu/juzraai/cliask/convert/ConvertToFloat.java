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
 * @author Zsolt Jurányi
 */
public class ConvertToFloat implements ConvertTo<Float> {

	@Override
	@Nonnull
	public Float convert(@Nonnull String rawValue) throws ConvertFailedException {
		try {
			return Float.parseFloat(rawValue);
		} catch (NumberFormatException e) {
			throw new ConvertFailedException("Invalid value for: float");
		}
	}
}
