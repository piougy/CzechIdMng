package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Entities on target system
 * 
 * @author Radek Tomiška
 *
 */
public interface SysSystemEntityService extends ReadWriteDtoService<SysSystemEntityDto, SystemEntityFilter>, ScriptEnabled {

	/**
	 * Returns {@link SysSystemEntity} by given system, entityType, and uid
	 * 
	 * @param uid
	 * @param entityType
	 * @return
	 */
	SysSystemEntityDto getBySystemAndEntityTypeAndUid(SysSystem system, SystemEntityType entityType, String uid);
	
}
