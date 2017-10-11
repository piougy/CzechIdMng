package eu.bcvsolutions.idm.core.api.utils;

import java.text.Normalizer;

import org.apache.commons.lang3.StringUtils;

/**
 * Spinal case string transformation
 * 
 * @author Radek Tomiška
 */
public abstract class SpinalCase {

	/**
	 * Returns transformed string as spinal case
	 * 
	 * @param input
	 * @return
	 */
	public static String format(String input) {
		if (StringUtils.isEmpty(input)) {
			return "";
		}
		String out = normalize(input);
		out = removeDuplicateWhiteSpaces(out);
		out = out.toLowerCase();
		return out.replace(' ', '-');
	}

	private static String normalize(String input) {
		String result = Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""); // 1
		return result.replaceAll("[^a-zA-Z0-9\\s]", " "); // 2
	}

	private static String removeDuplicateWhiteSpaces(String input) {
		return input.replaceAll("\\s+", " ");
	}
}