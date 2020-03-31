package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * Extra DTO for deserialization of entered DTO data. The standard embedded map
 * contains BaseDto as the value. It is an interface and cannot be deserialized
 * without type. This DTO maps the embedded data as JsonNode, so we can do the
 * next transformation with that.
 * 
 * @author Vít Švanda
 *
 */
public class EmbeddedDto implements BaseDto {

	private static final long serialVersionUID = 1L;

	@JsonDeserialize(as = UUID.class)
	private UUID id;
	@JsonProperty(value = "_embedded")
	private Map<String, JsonNode> embedded;

	@Override
	public Serializable getId() {
		return id;
	}

	@Override
	public void setId(Serializable id) {
		try {
			this.id = EntityUtils.toUuid(id);
		} catch (ClassCastException ex) {
			throw new IllegalArgumentException(
					"AbstractDto supports only UUID identifier. For different identifier generalize BaseEntity.", ex);
		}
	}

	public Map<String, JsonNode> getEmbedded() {
		return embedded;
	}

	public void setEmbedded(Map<String, JsonNode> embedded) {
		this.embedded = embedded;
	}

}