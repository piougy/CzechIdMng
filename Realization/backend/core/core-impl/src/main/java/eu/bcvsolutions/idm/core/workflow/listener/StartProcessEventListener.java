package eu.bcvsolutions.idm.core.workflow.listener;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Listener call after process started. Add automatically implementor as starter
 * and applicant as owner to new subprocess.
 * 
 * @author svandav
 *
 */
@Component
public class StartProcessEventListener implements ActivitiEventListener {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StartProcessEventListener.class);

	@Autowired
	private ApplicationContext context;

	@Override
	public void onEvent(ActivitiEvent event) {
		
		switch (event.getType()) {
			case PROCESS_STARTED: {
				LOG.debug("StartProcesEventListener - recieve event [{}]", event.getType());
	
				// Manual load bean ... autowire is not possible, because this listeners are
				// create before runtimeService
				RuntimeService runtimeService = context.getBean(RuntimeService.class);
				
				// To process set process instance ID as variable (we need use id in subprocess
				// and sometimes in parent process too)
				runtimeService.setVariable(event.getProcessInstanceId(), WorkflowProcessInstanceService.PROCESS_INSTANCE_ID,
						event.getProcessInstanceId());
				//
				break;
			}
			default: {
				LOG.debug("StartProcesEventListener - receive not required event [{}]", event.getType());
			}
		}
	}

	@Override
	public boolean isFailOnException() {
		// We can throw exception
		return true;
	}
}