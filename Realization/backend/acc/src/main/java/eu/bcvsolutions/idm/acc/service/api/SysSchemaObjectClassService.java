package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.filter.SchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Schema object class service
 * @author svandav
 *
 */
public interface SysSchemaObjectClassService extends ReadWriteEntityService<SysSchemaObjectClass, SchemaObjectClassFilter> {

}
