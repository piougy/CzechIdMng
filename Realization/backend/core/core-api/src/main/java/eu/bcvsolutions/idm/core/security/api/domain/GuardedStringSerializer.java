package eu.bcvsolutions.idm.core.security.api.domain;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Writes GuardedString proxy string to json
 * 
 * @author Radek Tomiška
 *
 */
public class GuardedStringSerializer extends JsonSerializer<GuardedString> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serialize(GuardedString value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException {
        jsonGenerator.writeString(GuardedString.SECRED_PROXY_STRING);
	}

}
