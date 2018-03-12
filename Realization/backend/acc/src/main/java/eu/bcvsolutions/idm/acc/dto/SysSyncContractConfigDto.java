package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;

/**
 * Default sync configuration DTO
 * 
 * @author svandav
 *
 */

@Relation(collectionRelation = "synchronizationConfigs")
public class SysSyncContractConfigDto extends AbstractSysSyncConfigDto {

	private static final long serialVersionUID = 1L;
	
	@Embedded(dtoClass = IdmTreeTypeDto.class)
	private UUID defaultTreeType;
	
	@Embedded(dtoClass = IdmTreeNodeDto.class)
	private UUID defaultTreeNode;
	
	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID defaultLeader;
	
	private boolean startOfHrProcesses = true;
	
	/*
	 * Start recalculation after end synchronization for automatic roles by attribute
	 */
	private boolean startAutoRoleRec = true;

	public UUID getDefaultTreeType() {
		return defaultTreeType;
	}

	public void setDefaultTreeType(UUID defaultTreeType) {
		this.defaultTreeType = defaultTreeType;
	}

	public UUID getDefaultTreeNode() {
		return defaultTreeNode;
	}

	public void setDefaultTreeNode(UUID defaultTreeNode) {
		this.defaultTreeNode = defaultTreeNode;
	}

	public UUID getDefaultLeader() {
		return defaultLeader;
	}

	public void setDefaultLeader(UUID defaultLeader) {
		this.defaultLeader = defaultLeader;
	}

	public boolean isStartOfHrProcesses() {
		return startOfHrProcesses;
	}

	public void setStartOfHrProcesses(boolean startOfHrProcesses) {
		this.startOfHrProcesses = startOfHrProcesses;
	}

	public boolean isStartAutoRoleRec() {
		return startAutoRoleRec;
	}

	public void setStartAutoRoleRec(boolean startAutoRoleRec) {
		this.startAutoRoleRec = startAutoRoleRec;
	}

}
