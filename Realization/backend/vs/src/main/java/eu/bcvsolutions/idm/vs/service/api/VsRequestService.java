package eu.bcvsolutions.idm.vs.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.vs.repository.filter.RequestFilter;
import eu.bcvsolutions.idm.vs.service.api.dto.VsRequestDto;

/**
 * Service for request in virtual system
 * 
 * @author Svanda
 *
 */
public interface VsRequestService extends 
		ReadWriteDtoService<VsRequestDto, RequestFilter>, AuthorizableService<VsRequestDto> {

}
