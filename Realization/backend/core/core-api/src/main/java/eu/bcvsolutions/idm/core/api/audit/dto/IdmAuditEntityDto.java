package eu.bcvsolutions.idm.core.api.audit.dto;

import java.util.Map;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * DTO that extends original {@link IdmAuditDto} with
 * version of entity. Entity is now stored in map.
 *
 * @author Ondrej Kopr
 *
 */
@Relation(collectionRelation = "audits")
public class IdmAuditEntityDto extends IdmAuditDto implements BaseDto {

	private static final long serialVersionUID = 1L;
	
	private Map<String, Object> entity;

	public Map<String, Object> getEntity() {
		return entity;
	}

	public void setEntity(Map<String, Object> entity) {
		this.entity = entity;
	}

	
}
