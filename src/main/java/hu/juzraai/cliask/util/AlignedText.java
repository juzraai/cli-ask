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

package hu.juzraai.cliask.util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Zsolt Jurányi
 */
public class AlignedText { // TODO doc, test

	private final int width;
	private final Align align;
	private final String s;

	public AlignedText(int width, Align align, @Nonnull String s) {
		this.width = width;
		this.align = align;
		this.s = align(s);
	}

	protected void addWord(@Nonnull String word, @Nonnull List<String> paragraphLines) {
		String lastLine = paragraphLines.get(paragraphLines.size() - 1);
		if (word.length() > width) { // word is longer than width
			int at = width - lastLine.length() - 1;
			if (at < 3) { // smallest starter chunk is 3 chr
				at = width; // if there's no room for it, cut a 'width' length word -> which will be started on new line (see below)
			}
			addWord(word.substring(0, at), paragraphLines);
			addWord(word.substring(at), paragraphLines);
		} else if (lastLine.length() + 1 + word.length() > width) { // word can't fit in current line
			paragraphLines.add(word); // put it into new line
		} else { // word can fit in current line
			paragraphLines.set(paragraphLines.size() - 1, String.format("%s %s", lastLine, word).trim()); // append to current line
		}
	}

	@Nonnull
	protected String align(@Nonnull String s) {

		// cleaning
		s = s.replaceAll("( +|\t|\r)", " ").replaceAll("\n\n+", "\n\n").trim();

		// build paragraphs
		StringBuilder r = new StringBuilder();
		for (String line : s.split("\n")) {
			r.append(buildParagraph(line));
		}
		return r.toString();
	}

	@Nonnull
	protected String alignLine(@Nonnull String s) {
		if (Align.LEFT != align) { // left align: needs no operation
			int alignWidth = Align.CENTER == align
					? width - (width - s.length()) / 2 // center align: right align in width W-((W-L)/2)
					: width; // right align: full width right align
			String f = String.format("%%%ds", alignWidth);
			s = String.format(f, s);
		}
		return s;
	}

	@Nonnull
	protected String buildParagraph(@Nonnull String line) {

		// build lines
		List<String> paragraphLines = new ArrayList<>(Arrays.asList(""));
		for (String word : line.split(" ")) {
			addWord(word, paragraphLines);
		}

		// join lines
		StringBuilder paragraph = new StringBuilder();
		for (String paragraphLine : paragraphLines) {
			paragraph.append(alignLine(paragraphLine));
			paragraph.append("\n");
		}

		return paragraph.toString();
	}

	@Override
	@Nonnull
	public String toString() {
		return s;
	}

	public enum Align {
		LEFT, CENTER, RIGHT
	}
}
