package eu.bcvsolutions.idm.core.api.jaxb;

import java.io.IOException;
import java.io.Writer;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

/**
 * Simple character escape encoder for JAXB. Without this encoder will be html
 * tags stored as escaping characters.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class JaxbCharacterEscapeEncoder implements CharacterEscapeHandler {

	@Override
	public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
		for (int i = start; i < start + length; i++) {
			char character = ch[i];
			out.write(character);
		}
	}

}
