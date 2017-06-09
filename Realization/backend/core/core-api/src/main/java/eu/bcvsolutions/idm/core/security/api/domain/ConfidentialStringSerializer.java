package eu.bcvsolutions.idm.core.security.api.domain;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Writes ConfidentialString proxy string to json
 * 
 * @author Radek Tomiška
 */
public class ConfidentialStringSerializer extends JsonSerializer<ConfidentialString> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serialize(ConfidentialString value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        jsonGenerator.writeString(GuardedString.SECRED_PROXY_STRING);
	}

}
