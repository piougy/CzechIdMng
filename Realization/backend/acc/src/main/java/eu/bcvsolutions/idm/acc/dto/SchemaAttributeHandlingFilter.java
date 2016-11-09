package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.core.api.dto.BaseFilter;

/**
 * Filter for attribute entity handling
 * 
 * @author Svanda
 *
 */
public class SchemaAttributeHandlingFilter implements BaseFilter {
	
	private Long entityHandlingId;
	
	private String idmPropertyName;

	public Long getEntityHandlingId() {
		return entityHandlingId;
	}

	public void setEntityHandlingId(Long entityHandlingId) {
		this.entityHandlingId = entityHandlingId;
	}

	public String getIdmPropertyName() {
		return idmPropertyName;
	}

	public void setIdmPropertyName(String idmPropertyName) {
		this.idmPropertyName = idmPropertyName;
	}
	
}
