package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Entities on target system
 * 
 * @author Radek Tomiška
 *
 */
public interface SysSystemEntityService extends ReadWriteEntityService<SysSystemEntity, SystemEntityFilter> {

}
