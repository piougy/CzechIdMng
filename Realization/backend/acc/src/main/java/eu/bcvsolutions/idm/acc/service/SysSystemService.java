package eu.bcvsolutions.idm.acc.service;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.model.dto.QuickFilter;
import eu.bcvsolutions.idm.core.model.service.IdentifiableByNameEntityService;
import eu.bcvsolutions.idm.core.model.service.ReadWriteEntityService;

/**
 * Target system configuration service 
 * 
 * @author Radek Tomiška
 *
 */
public interface SysSystemService extends ReadWriteEntityService<SysSystem, QuickFilter>, IdentifiableByNameEntityService<SysSystem> {

}
