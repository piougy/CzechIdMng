package eu.bcvsolutions.idm.core.api.domain;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * Json deserializer for BaseDto. Uses field '_type' for get DTO type.
 * 
 * @author Vít Švanda
 *
 */
public class BaseDtoDeserializer extends JsonDeserializer<BaseDto> {

	@Override
	public BaseDto deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		TreeNode readTree = p.getCodec().readTree(p);
		TreeNode type = readTree.get("_dtotype");
		if (type != null) {
			String typeStr = type.toString().replace("\"", "");
			try {
				@SuppressWarnings("unchecked")
				Class<? extends BaseDto> typeClass = (Class<? extends BaseDto>) Class.forName(typeStr);
				if (typeClass.isAnnotationPresent(Inheritable.class)) {
					// !!Workaround for sync inherited DTOs. There is problem with '_type' property
					// ... is not serialized. So here is sets back.
					((ObjectNode) readTree).put("_type", typeClass.getSimpleName());
				}
				return p.getCodec().treeToValue(readTree, typeClass);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

}
