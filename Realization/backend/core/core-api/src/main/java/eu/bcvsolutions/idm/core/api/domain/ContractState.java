package eu.bcvsolutions.idm.core.api.domain;

/**
 * Identity contract state
 * 
 * @author Radek Tomiška
 *
 */
public enum ContractState {

	/**
	 * Excluded from evidence - remains valid, but roles assigned for this contract are not added for logged identity.
	 * Excluded contract is not valid for managers - manager has to have valid not excluded contract.
	 */
	EXCLUDED(false),
	/**
	 * Invalid by user - not changed by dates
	 */
	DISABLED(true);

	private boolean disabled;

	private ContractState(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isDisabled() {
		return disabled;
	}
}
