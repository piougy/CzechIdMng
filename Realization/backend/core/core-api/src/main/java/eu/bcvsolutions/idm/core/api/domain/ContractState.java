package eu.bcvsolutions.idm.core.api.domain;

/**
 * Identity contract state
 * 
 * @author Radek Tomiška
 *
 */
public enum ContractState {

	EXCLUDED(false), // excluded from evidence - remains valid, but roles assigned for this contract are not added for logged identity
	DISABLED(true); // invalid by user - not changed by dates

	private boolean disabled;

	private ContractState(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isDisabled() {
		return disabled;
	}
}
