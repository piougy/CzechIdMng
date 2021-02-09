package eu.bcvsolutions.idm.core.api.domain;

/**
 * Identity state.
 * 
 * @author Radek Tomiška
 * @since 7.6.0
 */
public enum IdentityState {

	CREATED(false), 			// new identity - enabled by default (default contract can be disabled, and for backward compatibility)
	NO_CONTRACT(true), 			// identity doesn't have a contract
	FUTURE_CONTRACT(true),		// identity has future contract only
	VALID(false), 				// the only valid state - valid contracts
	LEFT(true), 				// all contract are invalid
	DISABLED(true), 			// automatically disabled ~ all contracts are excluded
	DISABLED_MANUALLY(true); 	// manually disabled - can be activated manually again

	private boolean disabled;

	private IdentityState(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isDisabled() {
		return disabled;
	}
}
