package eu.bcvsolutions.idm.core.security.api.domain;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Reads string as guarded string from json.
 * 
 * @author Radek Tomiška
 */
public class GuardedStringDeserializer extends JsonDeserializer<GuardedString> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GuardedString deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
		ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        return new GuardedString(node.asText());
	}

}
