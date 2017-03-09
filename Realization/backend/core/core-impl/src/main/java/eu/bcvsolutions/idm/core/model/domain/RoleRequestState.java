package eu.bcvsolutions.idm.core.model.domain;

/**
 * State of role request
 * @author svandav
 *
 */
public enum RoleRequestState {
	
	CREATED(false),
	EXECUTED(true),
	CANCELED(true),
	APPROVED(false),
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
