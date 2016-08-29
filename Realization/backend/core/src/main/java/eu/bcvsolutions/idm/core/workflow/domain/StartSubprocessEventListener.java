package eu.bcvsolutions.idm.core.workflow.domain;

import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiProcessStartedEvent;
import org.activiti.engine.task.IdentityLinkType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.repository.handler.IdmIdentityEventHandler;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Listener call after process started. Add automatically implementor as starter and applicant as owner to new subprocess.
 * @author svandav
 *
 */

@Component
public class StartSubprocessEventListener implements ActivitiEventListener {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdmIdentityEventHandler.class);

	@Autowired
	private AutowireCapableBeanFactory beanFactory;
	
	private RuntimeService runtimeService;

	@Override
	public void onEvent(ActivitiEvent event) {
		switch (event.getType()) {

		case PROCESS_STARTED:
			log.debug("StartSubprocesEventListener - receive event [{}]", event.getType());
			
			ActivitiProcessStartedEvent eventStarted = ((ActivitiProcessStartedEvent)event);
			if (eventStarted.getNestedProcessInstanceId()  == null) {
				// Only superprocess have nested process null;
				return;
			}
			// Manual load bean ... autowired is not possible, because this listeners are create before runtimeService 
			runtimeService = beanFactory.getBean(RuntimeService.class);
			@SuppressWarnings("unchecked") Map<String, Object> variables = eventStarted.getVariables();
			variables.forEach((k, v) -> {
				if (k.equals(WorkflowProcessInstanceService.APPLICANT_USERNAME)) {
					// Set applicant as owner of process
					runtimeService.addUserIdentityLink(event.getProcessInstanceId(), (String) v, IdentityLinkType.OWNER);
					log.debug("StartSubprocesEventListener - set process owner [{}]", v);
				} else if (k.equals(WorkflowProcessInstanceService.IMPLEMENTER_USERNAME)) {
					// Set current logged user (implementer) as starter of
					// process
					runtimeService.addUserIdentityLink(event.getProcessInstanceId(), (String) v, IdentityLinkType.STARTER);
					log.debug("StartSubprocesEventListener - set process starter [{}]", v);
				}
			});

			break;

		default:
			log.debug("StartSubprocesEventListener - receive not required event [{}]", event.getType());
		}
	}

	@Override
	public boolean isFailOnException() {
		// The logic in the onEvent method of this listener is not critical,
		// exceptions
		// can be ignored if logging fails...
		return false;
	}
}