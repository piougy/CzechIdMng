package eu.bcvsolutions.idm.acc.domain;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
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

	IDENTITY(IdmIdentityDto.class, true, true),
	ROLE(IdmRoleDto.class,true, true),
	TREE(IdmTreeNodeDto.class, true, true),
	ROLE_CATALOGUE(IdmRoleCatalogueDto.class, true, false),
	CONTRACT(IdmIdentityContractDto.class, false, true),
	CONTRACT_SLICE(IdmContractSliceDto.class, false, true);

	private Class<? extends AbstractDto> entityType;
	private boolean supportsProvisioning;
	private boolean supportsSync;

	private SystemEntityType(Class<? extends AbstractDto> entityType, boolean supportsProvisioning, boolean supportsSync) {
		this.entityType = entityType;
		this.supportsProvisioning = supportsProvisioning;
		this.supportsSync = supportsSync;
	}

	public Class<? extends AbstractDto> getEntityType() {
		return entityType;
	}
	

	public static SystemEntityType getByClass(Class<? extends AbstractDto> clazz) {
		for(SystemEntityType systemEntityType : SystemEntityType.values()){
			if(systemEntityType.getEntityType().equals(clazz)){
				return systemEntityType;
			}
		}
		return null;
	}

	public boolean isSupportsProvisioning() {
		return supportsProvisioning;
	}

	public boolean isSupportsSync() {
		return supportsSync;
	}
	
	
}
