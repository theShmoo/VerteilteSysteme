package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides utility methods for testing the solution.
 */
public final class TestUtils {

	private TestUtils() {
	}

	public static char[] repeat(char character, int count) {
		char[] bytes = new char[count];
		Arrays.fill(bytes, character);
		return bytes;
	}

	public static boolean contains(Object objectToFind, Object... array) {
		if (array != null && array.getClass().getComponentType().isInstance(objectToFind)) {
			for (int i = 0; i < array.length; i++) {
				if (objectToFind.equals(array[i])) {
					return true;
				}
			}
		}
		return false;
	}

	public static String join(String separator, List<String> strings) {
		StringBuilder appendable = new StringBuilder();
		if (strings != null && strings.size() > 0) {
			appendable.append(strings.get(0));
			for (int i = 1; i < strings.size(); i++) {
				appendable.append(separator).append(strings.get(i));
			}
		}
		return appendable.toString();
	}

	public static List<String> readLines(InputStream in, Charset cs) throws IOException {
		List<String> result = new ArrayList<String>();
		cs = cs != null ? cs : Charset.defaultCharset();
		BufferedReader lineReader = new BufferedReader(new InputStreamReader(in, cs));
		try {
			for (String line; ((line = lineReader.readLine()) != null); ) {
				result.add(line);
			}
		} finally {
			lineReader.close();
		}
		return result;
	}
}
