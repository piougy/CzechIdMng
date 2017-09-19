package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

/**
 * Filter for system entity handling
 * 
 * @author Svanda
 *
 */
public class SysSystemMappingFilter extends QuickFilter {
	
	private UUID systemId;
	private UUID objectClassId;
	private SystemOperationType operationType;
	private SystemEntityType entityType;
	private UUID treeTypeId;

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

	public SystemOperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(SystemOperationType operationType) {
		this.operationType = operationType;
	}

	public SystemEntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
	}

	public void setObjectClassId(UUID objectClassId) {
		this.objectClassId = objectClassId;
	}
	
	public UUID getObjectClassId() {
		return objectClassId;
	}

	public UUID getTreeTypeId() {
		return treeTypeId;
	}

	public void setTreeTypeId(UUID treeTypeId) {
		this.treeTypeId = treeTypeId;
	}
}
