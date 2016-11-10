package eu.bcvsolutions.idm.core.api.utils;

import java.text.Normalizer;

/**
 * Spinal case string transformation
 * 
 * @author Radek Tomiška
 *
 */
public class SpinalCase {

	private SpinalCase() {
	}

	/**
	 * Returns transformed string as spinal case
	 * 
	 * @param input
	 * @return
	 */
	public static String parse(String input) {
		if (isEmptyOrNull(input)) {
			return "";
		}
		String out = normalize(input);
		out = removeDuplicateWhiteSpaces(out);
		out = out.toLowerCase();
		out = out.replace(' ', '-');

		return out;
	}

	private static String normalize(String input) {
		String result = Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""); // 1
		result = result.replaceAll("[^a-zA-Z0-9\\s]", " "); // 2

		return result;
	}

	private static String removeDuplicateWhiteSpaces(String input) {
		return input.replaceAll("\\s+", " ");
	}

	private static boolean isEmptyOrNull(String input) {
		return (input == null || input.trim().length() == 0);
	}
}