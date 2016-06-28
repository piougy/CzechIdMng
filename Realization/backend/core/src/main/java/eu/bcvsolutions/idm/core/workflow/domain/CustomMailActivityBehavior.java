package eu.bcvsolutions.idm.core.workflow.domain;

import org.activiti.engine.impl.bpmn.behavior.MailActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Mail implementation
 * @author svanda
 */
public class CustomMailActivityBehavior extends MailActivityBehavior {

	private static final long serialVersionUID = 1L;
	private static final transient Logger logger = LoggerFactory.getLogger(CustomMailActivityBehavior.class);

	@Override
	public void execute(ActivityExecution execution) {
		logger.debug("MailActiviti executeId "+execution.getId());
		leave(execution);
	}

}
