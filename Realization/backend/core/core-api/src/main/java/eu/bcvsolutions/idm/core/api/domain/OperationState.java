package eu.bcvsolutions.idm.core.api.domain;

/**
 * Provisioning operation result
 * 
 * @author Radek Tomiška
 *
 */
public enum OperationState {
	CREATED, // newly created, not processed
	RUNNING, // running
	EXECUTED, // The operation was successfully executed
	EXCEPTION, // There was an exception during execution
	NOT_EXECUTED, // The operation was not executed because of some reason (in queue, readonly system ... etc)
	CANCELED; // canceled by some reason (administrator etc.) 
	
	/**
	 * Returns true, when task could ran
	 * 
	 * @param state
	 * @return
	 */
	public static boolean isRunnable(OperationState state) {
		return CREATED.equals(state) || RUNNING.equals(state);
	}
}