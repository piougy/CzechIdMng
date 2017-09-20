package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaObjectClassFilter;
import eu.bcvsolutions.idm.core.api.service.CloneableService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Schema object class service
 * @author svandav
 *
 */
public interface SysSchemaObjectClassService extends ReadWriteDtoService<SysSchemaObjectClassDto, SysSchemaObjectClassFilter>, CloneableService<SysSchemaObjectClassDto> {

}
