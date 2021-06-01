package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaObjectClassFilter;
import eu.bcvsolutions.idm.core.api.service.CloneableService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import java.util.UUID;

/**
 * Schema object class service
 * @author svandav
 *
 */
public interface SysSchemaObjectClassService extends ReadWriteDtoService<SysSchemaObjectClassDto, SysSchemaObjectClassFilter>, CloneableService<SysSchemaObjectClassDto> {

	/**
	 * Find first mapping for entity type and system, from the account and return his object class.
	 */
	IcObjectClass findByAccount(UUID system, SystemEntityType entityType);
}
