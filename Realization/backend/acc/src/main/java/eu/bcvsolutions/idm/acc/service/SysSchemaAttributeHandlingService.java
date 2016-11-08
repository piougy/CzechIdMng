package eu.bcvsolutions.idm.acc.service;

import eu.bcvsolutions.idm.acc.dto.SchemaAttributeHandlingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Schema attribute handling service
 * @author svandav
 *
 */
public interface SysSchemaAttributeHandlingService extends ReadWriteEntityService<SysSchemaAttributeHandling, SchemaAttributeHandlingFilter> {

}
