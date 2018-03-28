package eu.bcvsolutions.idm.core.api.domain;

/**
 * State of request (general state)
 * 
 * @author svandav
 *
 */
public enum RequestState {

	CONCEPT(false),
	EXECUTED(true),
	CANCELED(true),
	APPROVED(false),
	DISAPPROVED(true),
	IN_PROGRESS(false),
	EXCEPTION(true),
	DUPLICATED(true);
	
	private boolean terminatedState;
	
	private RequestState(boolean terminatedState) {
		this.terminatedState = terminatedState;
	}

	public boolean isTerminatedState() {
		return terminatedState;
	}
}
