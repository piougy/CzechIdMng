package eu.bcvsolutions.idm.vs.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.vs.repository.filter.VsRequestFilter;
import eu.bcvsolutions.idm.vs.service.api.dto.VsConnectorObjectDto;
import eu.bcvsolutions.idm.vs.service.api.dto.VsRequestDto;

/**
 * Service for request in virtual system
 * 
 * @author Svanda
 *
 */
public interface VsRequestService
		extends ReadWriteDtoService<VsRequestDto, VsRequestFilter>, AuthorizableService<VsRequestDto> {

	IcUidAttribute execute(VsRequestDto request);

	IcUidAttribute internalStart(VsRequestDto request);

	VsRequestDto createRequest(VsRequestDto request);

	IcUidAttribute internalExecute(VsRequestDto request);

	VsRequestDto realize(VsRequestDto request);

	VsRequestDto cancel(VsRequestDto request, String reason);

	/**
	 * Find duplicity requests. All request in state IN_PROGRESS for same UID
	 * and system. For all operation types.
	 * 
	 * @return
	 */
	List<VsRequestDto> findDuplicities(String uid, UUID systemId);

	/**
	 * Return account from connector. Account will be contained "wish"
	 * attributes ... it means current attributes + changed attributes from
	 * unresolved requests
	 * 
	 * @param request
	 * @return
	 */
	IcConnectorObject getConnectorObject(VsRequestDto request);

	/**
	 * Return account. Account will be contained only "valid" attributes (not
	 * from requests).
	 * 
	 * @param request
	 * @return
	 */
	IcConnectorObject getVsConnectorObject(VsRequestDto request);

	/**
	 * Read wish connector object. Object contains current attributes from
	 * virtual system + changed attributes from given request.
	 * 
	 * @param request
	 * @return
	 */
	VsConnectorObjectDto getWishConnectorObject(VsRequestDto request);

}
