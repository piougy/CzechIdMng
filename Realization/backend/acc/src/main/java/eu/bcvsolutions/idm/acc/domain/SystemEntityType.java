package eu.bcvsolutions.idm.acc.domain;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
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

	IDENTITY(IdmIdentityDto.class),
	ROLE(IdmRoleDto.class),
	TREE(IdmTreeNodeDto.class),
	ROLE_CATALOGUE(IdmRoleCatalogueDto.class);

	private Class<? extends AbstractDto> entityType;

	private SystemEntityType(Class<? extends AbstractDto> entityType) {
		this.entityType = entityType;
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

}
