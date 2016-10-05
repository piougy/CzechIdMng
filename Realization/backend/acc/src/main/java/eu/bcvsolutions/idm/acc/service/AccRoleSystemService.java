package eu.bcvsolutions.idm.acc.service;

import eu.bcvsolutions.idm.acc.dto.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccRoleSystem;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Role could assign identity accont on target system.
 * 
 * @author Radek Tomiška
 *
 */
public interface AccRoleSystemService extends ReadWriteEntityService<AccRoleSystem, RoleSystemFilter> {

}
