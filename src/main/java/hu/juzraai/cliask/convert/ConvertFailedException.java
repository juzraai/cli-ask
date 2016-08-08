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

/**
 * Exception type used in converters. They should gather all known exceptions
 * and produce a new instance of {@link ConvertFailedException}.
 *
 * @author Zsolt Jurányi
 */
public class ConvertFailedException extends Exception {

	/**
	 * Creates a new instance.
	 *
	 * @param message Exception message which describes the problem for the
	 *                user
	 */
	public ConvertFailedException(String message) {
		super(message);
	}
}
