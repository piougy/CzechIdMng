package eu.bcvsolutions.idm.acc.service.api;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.api.service.CloneableService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormValueService;

/**
 * Service for control form value on system
 * 
 * @author svandav
 *
 */
public interface SysSystemFormValueService extends FormValueService<SysSystem>, CloneableService<IdmFormValueDto> {

	/**
	 * Duplicate form value. Create and persist new form value (included confidential value save)
	 * @param id
	 * @param owner for this value
	 * @return
	 */
	IdmFormValueDto duplicate(UUID id, SysSystem owner);

}
