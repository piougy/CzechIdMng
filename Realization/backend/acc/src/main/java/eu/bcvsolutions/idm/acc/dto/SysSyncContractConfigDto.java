package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
}
