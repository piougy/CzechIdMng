package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.entity.SysConnectorServer;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

public interface SysConnectorServerService extends ReadWriteEntityService<SysConnectorServer, QuickFilter>, IdentifiableByNameEntityService<SysConnectorServer> {

}
