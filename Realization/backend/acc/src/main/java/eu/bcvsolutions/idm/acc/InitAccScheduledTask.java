package eu.bcvsolutions.idm.acc;

import java.io.InputStream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.event.processor.module.AccInitScheduledTaskProcessor;
import eu.bcvsolutions.idm.core.scheduler.config.BaseScheduledTaskInitializer;

/**
 * Acc long running tasks.
 * 
 * @see AccInitScheduledTaskProcessor
 * @author Radek Tomiška
 */
@Component
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", matchIfMissing = true)
public class InitAccScheduledTask extends BaseScheduledTaskInitializer {

	private static final String ACC_SCHEDULED_TASK_XML = "AccScheduledTasks.xml";
	
	@Override
	protected InputStream getTasksInputStream() {
		return this.getClass().getClassLoader().getResourceAsStream(getTasksXmlPath());
	}

	@Override
	protected String getTasksXmlPath() {
		return DEFAULT_RESOURCE + ACC_SCHEDULED_TASK_XML;
	}

}
