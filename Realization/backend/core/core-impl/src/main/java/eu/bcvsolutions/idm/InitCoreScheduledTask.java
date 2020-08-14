package eu.bcvsolutions.idm;

import java.io.InputStream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.event.processor.module.InitScheduledTaskProcessor;
import eu.bcvsolutions.idm.core.scheduler.config.BaseScheduledTaskInitializer;

/**
 * Implementation of {@link BaseScheduledTaskInitializer} for initial core
 * long running task.
 * 
 * @see InitScheduledTaskProcessor
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@Component
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", matchIfMissing = true)
public class InitCoreScheduledTask extends BaseScheduledTaskInitializer {

	private static final String CORE_SCHEDULED_TASK_XML = "CoreScheduledTasks.xml";
	
	@Override
	protected InputStream getTasksInputStream() {
		return this.getClass().getClassLoader().getResourceAsStream(getTasksXmlPath());
	}

	@Override
	protected String getTasksXmlPath() {
		return DEFAULT_RESOURCE + CORE_SCHEDULED_TASK_XML;
	}

}
