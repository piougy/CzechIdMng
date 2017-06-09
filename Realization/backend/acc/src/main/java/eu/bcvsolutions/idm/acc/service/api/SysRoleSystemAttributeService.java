package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.dto.filter.RoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Mapping attribute to system for role
 * 
 * @author svandav
 *
 */
public interface SysRoleSystemAttributeService extends ReadWriteEntityService<SysRoleSystemAttribute, RoleSystemAttributeFilter> {
	
	/**
	 * Convert method for SysRoleSystemAttribute to mapping attribute dto
	 * @param overloadingAttribute
	 * @param overloadedAttribute
	 */
	void fillOverloadedAttribute(SysRoleSystemAttribute overloadingAttribute, AttributeMapping overloadedAttribute);

}
