package eu.bcvsolutions.idm.core.api.utils;

import java.text.Normalizer;

import org.apache.commons.lang3.StringUtils;

/**
 * Spinal case string transformation.
 * Usable in urls.
 * 
 * @author Radek Tomiška
 */
public abstract class SpinalCase {

	/**
	 * Returns transformed string as spinal case.
	 * 
	 * @param input text
	 * @return url friendly string
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

	/**
	 * String ~ without special characters.
	 * 
	 * @param input text
	 * @return normalized text ~ without special characters
	 * @since 10.6.0
	 */
	public static String normalize(String input) {
		String result = Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""); // 1
		return result.replaceAll("[^a-zA-Z0-9\\s]", " "); // 2
	}

	/**
	 * String without duplicate white spaces.
	 * 
	 * @param input text
	 * @return normalized text ~ without duplicate white spaces
	 * @since 10.6.0
	 */
	public static String removeDuplicateWhiteSpaces(String input) {
		return input.replaceAll("\\s+", " ");
	}
}