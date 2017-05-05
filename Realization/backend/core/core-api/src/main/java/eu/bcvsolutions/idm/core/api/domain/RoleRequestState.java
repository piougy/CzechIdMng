package eu.bcvsolutions.idm.core.api.domain;

/**
 * State of role request
 * @author svandav
 *
 */
public enum RoleRequestState {

	CONCEPT(false),
	EXECUTED(true),
	CANCELED(true),
	APPROVED(false),
	DISAPPROVED(true),
	IN_PROGRESS(false),
	EXCEPTION(true),
	DUPLICATED(true);
	
	private boolean terminatedState;
	
	private RoleRequestState(boolean terminatedState) {
		this.terminatedState = terminatedState;
	}

	public boolean isTerminatedState() {
		return terminatedState;
	}
}
