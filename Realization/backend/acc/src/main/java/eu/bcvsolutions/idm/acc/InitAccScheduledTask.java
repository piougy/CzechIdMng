package eu.bcvsolutions.idm.acc;

import java.io.InputStream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.scheduler.config.AbstractScheduledTaskInitializer;

/**
 * Acc long running tasks
 * 
 * @author Radek Tomiška
 */
@Component
@DependsOn("initApplicationData")
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", matchIfMissing = true)
public class InitAccScheduledTask extends AbstractScheduledTaskInitializer {

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
