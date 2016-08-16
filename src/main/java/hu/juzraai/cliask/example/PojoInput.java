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

import hu.juzraai.cliask.AskFor;
import hu.juzraai.cliask.annotation.Ask;

/**
 * Example program which demonstrates how <code>AskFor.object(Object)</code>
 * works.
 *
 * @author Zsolt Jurányi
 */
public class PojoInput {

	/**
	 * Asks for an <code>ExamplePojo</code> object and prints out the returned
	 * value.
	 *
	 * @param args Command line arguments (ignored)
	 */
	public static void main(String[] args) {
		System.out.println(AskFor.object(new ExamplePojo()));
	}

	/**
	 * Example POJO class used in the demo program.
	 */
	public static class ExamplePojo {

		/**
		 * The prompt for this field will be the name of the field ("name").
		 */
		@Ask
		private String name;

		/**
		 * The input value will be converted automagically into
		 * <code>Byte</code> or reasked on error.
		 */
		@Ask("How old are you?")
		private Byte age;

		/**
		 * This field will not be asked, because it doesn't have
		 * <code>@Ask</code> annotation.
		 */
		private String skippedField = "untouched";

		/**
		 * This field shows that also primitives and boolean values are
		 * converted, and also default values work in POJO-mode too.
		 */
		@Ask("Are you sure?")
		private boolean sure = true;

		/**
		 * Used to print out the values for test purposes.
		 *
		 * @return Returns the classname and the values of fields.
		 */
		@Override
		public String toString() {
			return "ExamplePojo{" +
					"name='" + name + '\'' +
					", age=" + age +
					", skippedField='" + skippedField + '\'' +
					", sure=" + sure +
					'}';
		}
	}
}
