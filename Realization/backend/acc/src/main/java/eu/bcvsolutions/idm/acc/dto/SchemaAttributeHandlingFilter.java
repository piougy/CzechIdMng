package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.BaseFilter;

/**
 * Filter for attribute entity handling
 * 
 * @author Svanda
 *
 */
public class SchemaAttributeHandlingFilter implements BaseFilter {
	
	private UUID entityHandlingId;
	
	private String idmPropertyName;

	public UUID getEntityHandlingId() {
		return entityHandlingId;
	}

	public void setEntityHandlingId(UUID entityHandlingId) {
		this.entityHandlingId = entityHandlingId;
	}

	public String getIdmPropertyName() {
		return idmPropertyName;
	}

	public void setIdmPropertyName(String idmPropertyName) {
		this.idmPropertyName = idmPropertyName;
	}
	
}
