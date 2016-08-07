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

package hu.juzraai.cliask.example;

import hu.juzraai.cliask.core.AskFor;

/**
 * Example program which demonstrates how <code>AskFor.string(String,
 * String)</code> works.
 *
 * @author Zsolt Jurányi
 */
public class SimpleInput {

	/**
	 * Asks for 2 strings using <code>AskFor.string()</code>, one with default
	 * value and one without, then prints out values they returned.
	 *
	 * @param args Command line arguments (ignored)
	 */
	public static void main(String[] args) {

		// ask for string with default value
		// empty input will casue default value to be returned
		String s1 = AskFor.string("Test 1", "example");

		// ask for string without default value
		// AskFor will keep reasking till it gets non-empty input
		String s2 = AskFor.string("Test 2", null);

		// let's see what we got
		System.out.printf("Test 1 = %s%nTest 2 = %s%n", s1, s2);
	}
}
