package eu.bcvsolutions.idm.core.api.domain;

import org.springframework.core.PriorityOrdered;

/**
 * Task / event priority
 * 
 * @author Radek Tomiška
 * @see PriorityOrdered
 */
public enum PriorityType {

	IMMEDIATE(false, PriorityOrdered.HIGHEST_PRECEDENCE), // immediate ~ synchronously
	HIGH(true, 5), // high - asynchronously (7 / 10) in one cycle
	NORMAL(true, 10); // normal - asynchronously (3 / 10) in one cycle

	private final boolean async;
	private final int priority; // the smaller goes first

	private PriorityType(boolean async, int priority) {
		this.async = async;
		this.priority = priority;
	}

	public boolean isAsync() {
		return async;
	}
	
	public int getPriority() {
		return priority;
	}
}
