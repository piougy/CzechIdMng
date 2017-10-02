package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.core.api.service.CloneableService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Schema attribute service
 * @author svandav
 *
 */
public interface SysSchemaAttributeService extends ReadWriteDtoService<SysSchemaAttributeDto, SysSchemaAttributeFilter>, CloneableService<SysSchemaAttributeDto> {

}
