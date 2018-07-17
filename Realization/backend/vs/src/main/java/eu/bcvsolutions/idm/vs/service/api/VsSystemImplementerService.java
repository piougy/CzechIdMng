package eu.bcvsolutions.idm.vs.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.vs.dto.VsSystemImplementerDto;
import eu.bcvsolutions.idm.vs.dto.filter.VsSystemImplementerFilter;

/**
 * Service for system-implementer in virtual system
 * 
 * @author Svanda
 *
 */
public interface VsSystemImplementerService extends 
		ReadWriteDtoService<VsSystemImplementerDto, VsSystemImplementerFilter>, AuthorizableService<VsSystemImplementerDto> {

	/**
	 * Find all implementers for this system. Merge all identities and identities from all roles.
	 * Maximum of 1000 implementers are returned.
	 * 
	 * @param vsSystemId
	 * @return
	 */
	List<IdmIdentityDto> findRequestImplementers(UUID vsSystemId);
	
	/**
	 * Find all implementers for this system. Merge all identities and identities from all roles.
	 * @param vsSystemId
	 * @param limit - Max number of returned implementers
	 * @return
	 */
	List<IdmIdentityDto> findRequestImplementers(UUID vsSystemId, long limit);

}
