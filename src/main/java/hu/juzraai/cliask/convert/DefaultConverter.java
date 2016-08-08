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

import hu.juzraai.cliask.annotation.Ask;

import javax.annotation.Nonnull;

/**
 * This converter class is used only for default value in {@link Ask}
 * annotation, should not be used elsewhere.
 *
 * @author Zsolt Jurányi
 */
public class DefaultConverter implements ConvertTo<Object> {

	/**
	 * Throws {@link UnsupportedOperationException} because this class should
	 * not be used other than in {@link Ask} annotation as default value.
	 *
	 * @param rawValue Ignored
	 * @return Nothing, it throws {@link UnsupportedOperationException}
	 * exception
	 * @throws ConvertFailedException Never, it throws {@link UnsupportedOperationException}
	 */
	@Nonnull
	@Override
	public Object convert(@Nonnull String rawValue) throws ConvertFailedException {
		throw new UnsupportedOperationException("You should not use this converter!");
	}
}
