package eu.bcvsolutions.idm.acc.domain;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;

/**
 * Type of entity on target system
 * 
 * @author Radek Tomiška
 *
 */
public enum SystemEntityType {

	IDENTITY(IdmIdentity.class),
	ROLE(IdmRole.class),
	TREE(IdmTreeNode.class),
	ROLE_CATALOGUE(IdmRoleCatalogue.class);

	private Class<? extends AbstractEntity> entityType;

	private SystemEntityType(Class<? extends AbstractEntity> entityType) {
		this.entityType = entityType;
	}

	public Class<? extends AbstractEntity> getEntityType() {
		return entityType;
	}
	
	public static SystemEntityType getByClass(Class<? extends AbstractEntity> clazz) {
		for(SystemEntityType systemEntityType : SystemEntityType.values()){
			if(systemEntityType.getEntityType().equals(clazz)){
				return systemEntityType;
			}
		}
		return null;
	}

}
