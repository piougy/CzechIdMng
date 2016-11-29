package eu.bcvsolutions.idm.acc.domain;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Type of entity on target system
 * 
 * @author Radek Tomiška
 *
 */
public enum SystemEntityType {

	IDENTITY(IdmIdentity.class), GROUP(IdmRole.class);

	private Class<? extends AbstractEntity> entityType;

	private SystemEntityType(Class<? extends AbstractEntity> entityType) {
		this.entityType = entityType;
	}

	public Class<? extends AbstractEntity> getEntityType() {
		return entityType;
	}

}
