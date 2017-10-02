package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;

/**
 * DTO for entity {@link SysSystemMapping}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Relation(collectionRelation = "systemMappings")
public class SysSystemMappingDto extends AbstractDto {

	private static final long serialVersionUID = -3263064824050858302L;
	
	private String name;
	private SystemEntityType entityType;
	@Embedded(dtoClass = SysSchemaObjectClassDto.class)
	private UUID objectClass;
	private SystemOperationType operationType;
	@Embedded(dtoClass = IdmTreeTypeDto.class)
	private UUID treeType;
	private boolean protectionEnabled = false;
	private Integer protectionInterval;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SystemEntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
	}

	public UUID getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(UUID objectClass) {
		this.objectClass = objectClass;
	}

	public SystemOperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(SystemOperationType operationType) {
		this.operationType = operationType;
	}

	public UUID getTreeType() {
		return treeType;
	}

	public void setTreeType(UUID treeType) {
		this.treeType = treeType;
	}

	public boolean isProtectionEnabled() {
		return protectionEnabled;
	}

	public void setProtectionEnabled(boolean protectionEnabled) {
		this.protectionEnabled = protectionEnabled;
	}

	public Integer getProtectionInterval() {
		return protectionInterval;
	}

	public void setProtectionInterval(Integer protectionInterval) {
		this.protectionInterval = protectionInterval;
	}

}
