package eu.bcvsolutions.idm.vs.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.vs.dto.VsConnectorObjectDto;
import eu.bcvsolutions.idm.vs.dto.VsRequestDto;
import eu.bcvsolutions.idm.vs.dto.filter.VsRequestFilter;

/**
 * Service for request in virtual system
 * 
 * @author Svanda
 *
 */
public interface VsRequestService
		extends ReadWriteDtoService<VsRequestDto, VsRequestFilter>, AuthorizableService<VsRequestDto> {

	/**
	 * Execute new request. Publish event EXCECUTE.
	 * @param request
	 * @return
	 */
	IcUidAttribute execute(VsRequestDto request);

	/**
	 * Start of request - set state on In progress. Check duplicity and send notification.
	 * @param request
	 * @return
	 */
	IcUidAttribute internalStart(VsRequestDto request);

	/**
	 * Save request and set state to Concept.
	 * @param request
	 * @return
	 */
	VsRequestDto createRequest(VsRequestDto request);

	/**
	 * Internal execution. Propagate change in request to VsAccount (call connector).
	 * @param request
	 * @return
	 */
	IcUidAttribute internalExecute(VsRequestDto request);

	/**
	 * Realize request. Request will be marked as realized (only change state).
	 * @param request
	 * @return
	 */
	VsRequestDto realize(VsRequestDto request);

	/**
	 * Cancel request. Reason must be not null. Request will be only mark as cancel (change state)
	 * @param request
	 * @param reason
	 * @return
	 */
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
