package eu.bcvsolutions.idm.acc.service;

import eu.bcvsolutions.idm.acc.dto.SystemEntityHandlingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * System entity handling service
 * @author svandav
 *
 */
public interface SysSystemEntityHandlingService extends ReadWriteEntityService<SysSystemEntityHandling, SystemEntityHandlingFilter> {

}
