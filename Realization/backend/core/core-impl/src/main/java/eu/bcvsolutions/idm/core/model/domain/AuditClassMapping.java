package eu.bcvsolutions.idm.core.model.domain;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * Enum for aduiting class mapping
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public enum AuditClassMapping {

	IDM_ROLE(IdmRole.class),
	IDM_IDENTITY(IdmIdentity.class),
	IDM_TREE_NODE(IdmTreeNode.class),
	IDM_TREE_TYPE(IdmTreeType.class);
	
	private final Class<? extends BaseEntity> clazz;
	

	private AuditClassMapping(Class<? extends BaseEntity> clazz) {
		this.clazz =  clazz;
	}

	public Class<? extends BaseEntity> getClazz() {
		return clazz;
	}
	
	public String getName() {
		return this.getClazz().getName();
	}
	
}
