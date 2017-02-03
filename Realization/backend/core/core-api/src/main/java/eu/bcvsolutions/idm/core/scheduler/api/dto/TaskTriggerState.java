package eu.bcvsolutions.idm.core.scheduler.api.dto;

import org.quartz.Trigger.TriggerState;

/**
 * State of task trigger
 */
public enum TaskTriggerState {

	ACTIVE,
	PAUSED;
	// TODO: other states NONE, NORMAL, PAUSED, COMPLETE, ERROR, BLOCKED
	
	/**
	 * Converts state of trigger to state of task trigger
	 * 
	 * @param state state of trigger
	 * @return task trigger state
	 */
	public static TaskTriggerState convert(TriggerState state) {
		return TriggerState.PAUSED.equals(state) ? PAUSED : ACTIVE; 
	}
}
