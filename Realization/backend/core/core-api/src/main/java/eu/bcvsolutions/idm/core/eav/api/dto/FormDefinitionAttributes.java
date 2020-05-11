package eu.bcvsolutions.idm.core.eav.api.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Simple form definition with attributes.
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
public class FormDefinitionAttributes implements Serializable {

	private static final long serialVersionUID = 1L;
	//
	private UUID definition;
	private List<UUID> attributes;
	
	public UUID getDefinition() {
		return definition;
	}
	
	public void setDefinition(UUID definition) {
		this.definition = definition;
	}
	
	public List<UUID> getAttributes() {
		if (attributes == null) {
			attributes = new ArrayList<>();
		}
		return attributes;
	}
	
	public void setAttributes(List<UUID> attributes) {
		this.attributes = attributes;
	}
}
