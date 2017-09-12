package eu.bcvsolutions.idm.vs.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.vs.repository.filter.VsRequestImplementerFilter;
import eu.bcvsolutions.idm.vs.service.api.dto.VsRequestDto;
import eu.bcvsolutions.idm.vs.service.api.dto.VsRequestImplementerDto;

/**
 * Service for request-implementer in virtual system
 * 
 * @author Svanda
 *
 */
public interface VsRequestImplementerService extends 
		ReadWriteDtoService<VsRequestImplementerDto, VsRequestImplementerFilter>, AuthorizableService<VsRequestImplementerDto> {

	List<IdmIdentityDto> findRequestImplementers(VsRequestDto request);

}
