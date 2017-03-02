package app.editors;

// adopted from: https://github.com/vincent-zurczak/Xml-Region-Analyzer/
// see the original license:

/****************************************************************************
 *
 * Copyright (c) 2012, Vincent Zurczak - All rights reserved.
 * This source file is released under the terms of the BSD license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of California, Berkeley nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *****************************************************************************/

import java.util.ArrayList;
import java.util.List;

enum TokenType {
	INSTRUCTION,

	COMMENT,

	CDATA,

	MARKUP,

	ATTRIBUTE,

	MARKUP_VALUE,

	ATTRIBUTE_VALUE,

	WHITESPACE,

	UNEXPECTED;
}

class Token {
	final TokenType type;
	final int start;
	int end;

	Token(TokenType type, int start) {
		this.type = type;
		this.start = start;
	}

	Token(TokenType type, int start, int end) {
		this.type = type;
		this.start = start;
		this.end = end;
	}

}

class XmlTokenizer {

	private int offset;

	public List<Token> parse(String xml) {

		this.offset = 0;
		List<Token> positions = new ArrayList<Token>();
		while (this.offset < xml.length()) {

			// White spaces
			analyzeWhitespaces(xml, positions);
			if (this.offset >= xml.length())
				break;

			// "<" can be several things
			char c = xml.charAt(this.offset);
			if (c == '<') {
				if (analyzeInstruction(xml, positions))
					continue;
				if (analyzeComment(xml, positions))
					continue;
				if (analyzeMarkup(xml, positions))
					continue;
				if (analyzeCData(xml, positions))
					continue;

				positions.add(new Token(TokenType.UNEXPECTED,
						this.offset, xml.length()));
				break;
			}

			// "/" and "/>" can only indicate a mark-up
			else if (c == '/' && xml.charAt(this.offset + 1) == '>'
					|| c == '>') {
				if (analyzeMarkup(xml, positions))
					continue;

				positions.add(new Token(TokenType.UNEXPECTED,
						this.offset, xml.length()));
				break;
			}

			// Other things can be...
			if (analyzeAttribute(xml, positions))
				continue;
			if (analyzeAttributeValue(xml, positions))
				continue;
			if (analyzeMarkupValue(xml, positions))
				continue;

			positions.add(new Token(TokenType.UNEXPECTED, this.offset,
					xml.length()));
			break;
		}

		return positions;
	}

	boolean analyzeInstruction(String xml, List<Token> positions) {

		boolean result = false;
		int newPos = this.offset;
		if (newPos < xml.length()
				&& xml.charAt(newPos) == '<'
				&& ++newPos < xml.length()
				&& xml.charAt(newPos) == '?') {

			while (++newPos < xml.length()
					&& xml.charAt(newPos) != '>')
				newPos = xml.indexOf('?', newPos);

			if (xml.charAt(newPos) == '>') {
				positions.add(new Token(TokenType.INSTRUCTION,
						this.offset, newPos + 1));
				this.offset = newPos + 1;
				result = true;
			}
		}

		return result;
	}

	boolean analyzeComment(String xml, List<Token> positions) {

		boolean result = false;
		int newPos = this.offset;
		if (xml.charAt(newPos) == '<'
				&& ++newPos < xml.length()
				&& xml.charAt(newPos) == '!'
				&& ++newPos < xml.length()
				&& xml.charAt(newPos) == '-'
				&& ++newPos < xml.length()
				&& xml.charAt(newPos) == '-') {

			int seq = 0;
			while (seq != 3
					&& ++newPos < xml.length()) {
				char c = xml.charAt(newPos);
				seq = c == '-' && seq < 2 || c == '>' && seq == 2 ? seq + 1 : 0;
			}

			if (seq == 3)
				newPos++;

			positions.add(
					new Token(TokenType.COMMENT, this.offset, newPos));
			this.offset = newPos;
			result = true;
		}

		return result;
	}

	boolean analyzeMarkup(String xml, List<Token> positions) {

		int newPos = this.offset;
		boolean result = false;

		// "<..."
		if (xml.charAt(newPos) == '<') {

			// Do not process a CData section or a comment as a mark-up
			if (newPos + 1 < xml.length()
					&& xml.charAt(newPos + 1) == '!')
				return false;

			// Mark-up name
			char c = '!';
			while (newPos < xml.length()
					&& (c = xml.charAt(newPos)) != '>'
					&& !Character.isWhitespace(c))
				newPos++;

			if (c == '>')
				newPos++;

			positions.add(
					new Token(TokenType.MARKUP, this.offset, newPos));
			this.offset = newPos;
			result = true;
		}

		// "/>"
		else if (xml.charAt(newPos) == '/'
				&& ++newPos < xml.length()
				&& xml.charAt(newPos) == '>') {

			positions.add(
					new Token(TokenType.MARKUP, this.offset, ++newPos));
			this.offset = newPos;
			result = true;
		}

		// "attributes... >"
		else if (xml.charAt(newPos) == '>') {
			positions.add(
					new Token(TokenType.MARKUP, this.offset, ++newPos));
			this.offset = newPos;
			result = true;
		}

		return result;
	}

	boolean analyzeAttribute(String xml, List<Token> positions) {

		// An attribute value follows a mark-up
		for (int i = positions.size() - 1; i >= 0; i--) {
			Token xr = positions.get(i);
			if (xr.type == TokenType.WHITESPACE)
				continue;

			if (xr.type == TokenType.ATTRIBUTE_VALUE)
				break;

			if (xr.type == TokenType.MARKUP) {
				char c = xml.charAt(xr.end - 1);
				if (c != '>')
					break;
			}

			return false;
		}

		// Analyze what we have...
		boolean result = false;
		int newPos = this.offset;
		char c;
		while (newPos < xml.length()
				&& (c = xml.charAt(newPos)) != '='
				&& c != '/'
				&& c != '>'
				&& !Character.isWhitespace(c))
			newPos++;

		// Found one?
		if (newPos != this.offset) {
			positions.add(new Token(TokenType.ATTRIBUTE, this.offset,
					newPos));
			this.offset = newPos;
			result = true;
		}

		return result;
	}

	boolean analyzeMarkupValue(String xml, List<Token> positions) {

		// A mark-up value follows a mark-up
		for (int i = positions.size() - 1; i >= 0; i--) {
			Token xr = positions.get(i);
			if (xr.type == TokenType.WHITESPACE)
				continue;

			if (xr.type == TokenType.MARKUP
					|| xr.type == TokenType.COMMENT) {
				char c = xml.charAt(xr.end - 1);
				if (c == '>')
					break;
			}

			return false;
		}

		// Read...
		boolean result = false;
		int newPos = this.offset;
		while (newPos < xml.length()
				&& xml.charAt(newPos) != '<')
			newPos++;

		// We read something and this something is not only made up of white
		// spaces
		if (this.offset != newPos) {

			// We must here repair the list if the previous position is made up
			// of white spaces
			Token xr = positions.get(positions.size() - 1);
			int start = this.offset;
			if (xr.type == TokenType.WHITESPACE) {
				start = xr.start;
				positions.remove(xr);
			}

			positions.add(
					new Token(TokenType.MARKUP_VALUE, start, newPos));
			this.offset = newPos;
			result = true;
		}

		return result;
	}

	boolean analyzeAttributeValue(String xml, List<Token> positions) {

		// An attribute value follows an attribute
		for (int i = positions.size() - 1; i >= 0; i--) {
			Token xr = positions.get(i);
			if (xr.type == TokenType.WHITESPACE)
				continue;

			if (xr.type == TokenType.ATTRIBUTE)
				break;

			return false;
		}

		// Analyze what we have
		boolean result = false;
		int newPos = this.offset;
		if (xml.charAt(newPos) == '=') {
			analyzeWhitespaces(xml, positions);

			int cpt = 0;
			char previous = '!';
			while (++newPos < xml.length()) {
				char c = xml.charAt(newPos);
				if (previous != '\\' && c == '"')
					cpt++;

				previous = c;
				if (cpt == 2) {
					newPos++;
					break;
				}
			}

			positions.add(new Token(TokenType.ATTRIBUTE_VALUE,
					this.offset, newPos));
			this.offset = newPos;
			result = true;
		}

		return result;
	}

	boolean analyzeCData(String xml, List<Token> positions) {

		boolean result = false;
		int newPos = this.offset;
		if (xml.charAt(newPos) == '<'
				&& ++newPos < xml.length()
				&& xml.charAt(newPos) == '!'
				&& ++newPos < xml.length()
				&& xml.charAt(newPos) == '['
				&& ++newPos < xml.length()
				&& xml.charAt(newPos) == 'C'
				&& ++newPos < xml.length()
				&& xml.charAt(newPos) == 'D'
				&& ++newPos < xml.length()
				&& xml.charAt(newPos) == 'A'
				&& ++newPos < xml.length()
				&& xml.charAt(newPos) == 'T'
				&& ++newPos < xml.length()
				&& xml.charAt(newPos) == 'A'
				&& ++newPos < xml.length()
				&& xml.charAt(newPos) == '[') {

			int cpt = 0;
			while (++newPos < xml.length()) {
				char c = xml.charAt(newPos);
				if (cpt < 2 && c == ']'
						|| cpt == 2 && c == '>')
					cpt++;
				else
					cpt = 0;

				if (cpt == 3) {
					newPos++;
					break;
				}
			}

			positions.add(
					new Token(TokenType.CDATA, this.offset, newPos));
			this.offset = newPos;
			result = true;
		}

		return result;
	}

	void analyzeWhitespaces(String xml, List<Token> positions) {

		int i = this.offset;
		while (i < xml.length()
				&& Character.isWhitespace(xml.charAt(i)))
			i++;

		if (i != this.offset) {
			positions.add(
					new Token(TokenType.WHITESPACE, this.offset, i));
			this.offset = i;
		}
	}
}
