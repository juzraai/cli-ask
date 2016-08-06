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
public class ConvertToBoolean implements ConvertTo<Boolean> {

	private static final String TRUE_PATTERN = "TRUE|YES|ON|1";
	private static final String FALSE_PATTERN = "FALSE|NO|OFF|0";

	@Override
	@Nonnull
	public Boolean convert(@Nonnull String rawValue) throws ConvertFailedException {
		if (rawValue.toUpperCase().matches(TRUE_PATTERN)) {
			return Boolean.TRUE;
		}
		if (rawValue.toUpperCase().matches(FALSE_PATTERN)) {
			return Boolean.FALSE;
		}
		throw new ConvertFailedException(String.format("Invalid boolean value, specify one of these: %s|%s", TRUE_PATTERN, FALSE_PATTERN));
	}
}
