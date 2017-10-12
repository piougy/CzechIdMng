package eu.bcvsolutions.idm.acc.domain;

import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncContractConfigDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;

/**
 * Type of entity on target system
 * - its mapped to dto by underlying entity
 * 
 * @author Radek Tomiška
 *
 */
public enum SystemEntityType {

	IDENTITY(IdmIdentityDto.class, SysSyncConfigDto.class),
	ROLE(IdmRoleDto.class, SysSyncConfigDto.class),
	TREE(IdmTreeNodeDto.class, SysSyncConfigDto.class),
	ROLE_CATALOGUE(IdmRoleCatalogueDto.class, SysSyncConfigDto.class),
	CONTRACT(IdmIdentityContractDto.class, SysSyncContractConfigDto.class);

	private Class<? extends AbstractDto> entityType;
	private Class<? extends AbstractSysSyncConfigDto> syncConfigType;

	private SystemEntityType(Class<? extends AbstractDto> entityType, Class<? extends AbstractSysSyncConfigDto> syncConfigType) {
		this.entityType = entityType;
		this.syncConfigType = syncConfigType;
	}

	public Class<? extends AbstractDto> getEntityType() {
		return entityType;
	}
	
	public Class<? extends AbstractSysSyncConfigDto> getSyncConfigType() {
		return syncConfigType;
	}

	public static SystemEntityType getByClass(Class<? extends AbstractDto> clazz) {
		for(SystemEntityType systemEntityType : SystemEntityType.values()){
			if(systemEntityType.getEntityType().equals(clazz)){
				return systemEntityType;
			}
		}
		return null;
	}

}
