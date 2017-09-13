package eu.bcvsolutions.idm.vs.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.vs.repository.filter.VsSystemImplementerFilter;
import eu.bcvsolutions.idm.vs.service.api.dto.VsRequestDto;
import eu.bcvsolutions.idm.vs.service.api.dto.VsSystemImplementerDto;

/**
 * Service for system-implementer in virtual system
 * 
 * @author Svanda
 *
 */
public interface VsSystemImplementerService extends 
		ReadWriteDtoService<VsSystemImplementerDto, VsSystemImplementerFilter>, AuthorizableService<VsSystemImplementerDto> {

	List<IdmIdentityDto> findRequestImplementers(VsRequestDto request);

}
