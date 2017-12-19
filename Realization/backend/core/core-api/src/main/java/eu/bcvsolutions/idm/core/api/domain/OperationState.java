package eu.bcvsolutions.idm.core.api.domain;

/**
 * Provisioning operation result
 *
 * @author Radek Tomiška
 */
public enum OperationState {
	
	CREATED, // newly created, not processed
	RUNNING, // running
	EXECUTED, // The operation was successfully executed
	EXCEPTION, // There was an exception during execution
	NOT_EXECUTED, // The operation was not executed because of some reason (in queue, something is disabled, dry run ... etc)
	BLOCKED, // The operation was blocked  (e.g something is disabled)
	CANCELED; // canceled by some reason (administrator etc.)

	/**
	 * Returns true, when task could ran
	 *
	 * @param state
	 * @return
	 */
	public static boolean isRunnable(OperationState state) {
		return CREATED == state || RUNNING == state;
	}

	public static boolean isSuccessful(OperationState state) {
		return EXECUTED == state;
	}
}
