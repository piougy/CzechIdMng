package eu.bcvsolutions.idm.core.model.event.processor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.runtime.ProcessInstance;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Approvable event processing. Suspends current event processing and starts workflow defined by {@link #getWorkflowDefinitionKey()}.
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link BaseEntity}, {@link BaseDto} or any other {@link Serializable} content type
 */
public abstract class AbstractApprovableEventProcessor<E extends Serializable> extends AbstractWorkflowEventProcessor<E> {
	
	public static final String WF_VARIABLE_SKIP_APPROVING = "skipApproving";
	
	public AbstractApprovableEventProcessor(EventType... type) {
		super(type);
	}
	
	@Override
	public EventResult<E> process(EntityEvent<E> event) {
		Map<String, Object> variables = new HashMap<>();
		variables.put(EntityEvent.EVENT_PROPERTY, event);
		//
		ProcessInstance processInstance = processInstance(variables);
		//
		boolean suspend = true;
		if (processInstance.isEnded()) {
			suspend = false;
		} else if (processInstance instanceof VariableScope) {
			Object skipApproving = ((VariableScope) processInstance).getVariable(WF_VARIABLE_SKIP_APPROVING);
			if (skipApproving instanceof Boolean && Boolean.TRUE.equals(skipApproving)) {
				suspend = false;
			}
		}
		//
		return new DefaultEventResult
				.Builder<>(event, this)
				.setSuspended(suspend)
				.setResult(getResult(processInstance))
				.build();
	}
}
