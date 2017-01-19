package eu.bcvsolutions.idm.acc.domain;

/**
 * Provisioning operation result
 * 
 * @author Radek Tomiška
 *
 */
public enum ResultState {
	CREATED, // newly created, not processed
	EXECUTED, // The operation was successfully executed
	EXCEPTION, // There was an exception during execution
	NOT_EXECUTED; // The operation was not executed because of some reason (in queue, readonly system ... etc)
}