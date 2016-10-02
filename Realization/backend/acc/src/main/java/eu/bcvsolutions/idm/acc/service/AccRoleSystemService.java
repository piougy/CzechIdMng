package eu.bcvsolutions.idm.acc.service;

import eu.bcvsolutions.idm.acc.dto.AccRoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccRoleSystem;
import eu.bcvsolutions.idm.core.model.service.ReadWriteEntityService;

/**
 * Role could assign identity accont on target system.
 * 
 * @author Radek Tomiška
 *
 */
public interface AccRoleSystemService extends ReadWriteEntityService<AccRoleSystem, AccRoleSystemFilter> {

}
