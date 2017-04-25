package eu.bcvsolutions.idm.core.model.event.processor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Approvable event processing. Suspends current event processing and starts workflow defined by {@link #getWorkflowDefinitionKey()}.
 * 
 * @author Radek Tomiška
 *
 * @param <E> {@link BaseEntity}, {@link BaseDto} or any other {@link Serializable} content type
 */
public abstract class AbstractApprovableEventProcessor<E extends Serializable> extends AbstractEntityEventProcessor<E> {
	
	public static final String PROPERTY_WF = "wf";
	public static final String WF_VARIABLE_SKIP_APPROVING = "skipApproving";
	
	@Autowired
	private WorkflowProcessInstanceService workflowProcessInstanceService;
	@Autowired
	private SecurityService securityService;
	
	@Autowired
	public AbstractApprovableEventProcessor(EventType... type) {
		super(type);
	}
	
	public String getWorkflowDefinitionKey() {
		return getConfigurationProperty(PROPERTY_WF);
	}
	
	@Override
	public EventResult<E> process(EntityEvent<E> event) {
		Map<String, Object> variables = new HashMap<>();
		variables.put(EntityEvent.EVENT_PROPERTY, event);
		//
		IdmIdentityDto modifier = securityService.getAuthentication().getCurrentIdentity();
		ProcessInstance processInstance = workflowProcessInstanceService.startProcess(
				getWorkflowDefinitionKey(),
				modifier.getClass().getSimpleName(), 
				modifier.getUsername(), 
				modifier.getId().toString(), 
				variables);
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
		DefaultEventResult<E> result = new DefaultEventResult<>(event, this);
		result.setSuspended(suspend);
		return result;
	}
	
	/**
	 * If wf is not given, then this processor is disabled.
	 */
	@Override
	public boolean isDisabled() {
		if (super.isDisabled()) {
			return true;
		}
		return StringUtils.isEmpty(getWorkflowDefinitionKey());		
	}
	
	/**
	 * Adds wf property
	 */
	@Override
	public List<String> getPropertyNames() {
		List<String> properties =  super.getPropertyNames();
		properties.add(PROPERTY_WF);
		return properties;		
	}
}
