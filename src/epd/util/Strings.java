package epd.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Contains helper methods for string operations.
 */
public class Strings {

	private Strings() {
	}

	public static String trim(String s) {
		if (s == null)
			return null;
		return s.trim();
	}

	/**
	 * Cut a string to the given length. Appends "..." if the string was
	 * truncated.
	 */
	public static String cut(String string, int length) {

		if (string == null || length <= 0)
			return "";

		String str = string.trim();
		if (str.length() <= length)
			return str;

		return switch (length) {
			case 1 -> ".";
			case 2 -> "..";
			default -> str.substring(0, length - 3).concat("...");
		};
	}

	public static String[] readLines(InputStream is) throws IOException {
		if (is == null)
			return new String[0];

		List<String> list = new ArrayList<>();
		InputStreamReader reader = new InputStreamReader(is);
		try (BufferedReader buffer = new BufferedReader(reader)) {
			String line = null;
			while ((line = buffer.readLine()) != null) {
				list.add(line);
			}
		}
		return list.toArray(new String[0]);
	}

	/**
	 * Returns true if both of the given strings are null or if both of the
	 * given strings are equal.
	 */
	public static boolean nullOrEqual(String string1, String string2) {
		return (string1 == null && string2 == null)
			|| (string1 != null && string1.equals(string2));
	}

	/**
	 * Returns true if the given string value is null or empty.
	 */
	public static boolean nullOrEmpty(String val) {
		if (val == null)
			return true;
		return val.isBlank();
	}

	/**
	 * Returns true if the string is not null or empty, means that it contains
	 * other characters that white-spaces.
	 */
	public static boolean notEmpty(String val) {
		if (val == null)
			return false;
		String str = val.trim();
		return !str.isEmpty();
	}

	/**
	 * A null-save method for comparing two strings ignoring the case.
	 */
	public static int compare(String str1, String str2) {
		if (str1 == null && str2 == null)
			return 0;
		if (str1 != null && str2 == null)
			return 1;
		if (str1 == null)
			return -1;
		return str1.compareToIgnoreCase(str2);
	}

	public static <T> String join(Collection<T> values, char delimiter) {
		String[] stringValues = new String[values.size()];
		int i = 0;
		for (T value : values)
			if (value != null)
				stringValues[i++] = value.toString();
		return join(stringValues, delimiter);
	}

	public static String join(String[] values, char delimiter) {
		int length = 0;
		for (String v : values)
			if (v != null)
				length += v.length();
		StringBuilder b = new StringBuilder(length + values.length - 1);
		for (int i = 0; i < values.length; i++) {
			if (i != 0)
				b.append(delimiter);
			if (values[i] != null)
				b.append(values[i]);
		}
		return b.toString();
	}

	public static String wrap(String text, int len) {
		if (text == null)
			return "";
		String[] words = text.split("\\s");
		StringBuilder s = new StringBuilder();
		StringBuilder line = new StringBuilder();
		for (String w : words) {
			if (!line.isEmpty()
				&& (line.length() + w.length() > len)) {
				if (!s.isEmpty()) {
					s.append('\n');
				}
				s.append(line);
				line = new StringBuilder();
			}
			if (!line.isEmpty()) {
				line.append(' ');
			}
			line.append(w);
		}
		if (!line.isEmpty()) {
			if (!s.isEmpty()) {
				s.append('\n');
			}
			s.append(line);
		}
		return s.toString();
	}
}
