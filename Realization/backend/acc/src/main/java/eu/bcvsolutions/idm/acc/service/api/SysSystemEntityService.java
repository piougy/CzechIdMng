package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.filter.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Entities on target system
 * 
 * @author Radek Tomiška
 *
 */
public interface SysSystemEntityService extends ReadWriteEntityService<SysSystemEntity, SystemEntityFilter> {

	/**
	 * Returns {@link SysSystemEntity} by given system, entityType, and uid
	 * 
	 * @param uid
	 * @param entityType
	 * @return
	 */
	SysSystemEntity getBySystemAndEntityTypeAndUid(SysSystem system, SystemEntityType entityType, String uid);
	
}
