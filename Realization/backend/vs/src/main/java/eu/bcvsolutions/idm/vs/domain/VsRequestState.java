package eu.bcvsolutions.idm.vs.domain;

/**
 * State of request on virtual system
 * 
 * @author svandav
 *
 */
public enum VsRequestState {
	
	CONCEPT,
	CANCELED,
	REALIZED,
	REJECTED,
	IN_PROGRESS,
	EXCEPTION,
	DUPLICATED;
}
