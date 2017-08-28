package eu.bcvsolutions.idm.vs.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.vs.repository.filter.RequestBatchFilter;
import eu.bcvsolutions.idm.vs.service.api.dto.VsRequestBatchDto;

/**
 * Service for batch request in virtual system
 * 
 * @author Svanda
 *
 */
public interface VsRequestBatchService extends 
		ReadWriteDtoService<VsRequestBatchDto, RequestBatchFilter>, AuthorizableService<VsRequestBatchDto> {

}
