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

	public Long getEntityHandlingId() {
		return entityHandlingId;
	}

	public void setEntityHandlingId(Long entityHandlingId) {
		this.entityHandlingId = entityHandlingId;
	}
}
