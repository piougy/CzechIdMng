package eu.bcvsolutions.idm;

import java.io.InputStream;

import org.slf4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.scheduler.config.AbstractScheduledTaskInitializer;

/**
 * Implementation of {@link AbstractScheduledTaskInitializer} for initial core
 * long running task.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@DependsOn("initApplicationData")
public class InitCoreScheduledTask extends AbstractScheduledTaskInitializer {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitCoreScheduledTask.class);
	private static final String CORE_SCHEDULED_TASK_XML = "IdmCoreScheduledTasks.xml";
	
	@Override
	protected InputStream getTasksInputStream() {
		return this.getClass().getClassLoader().getResourceAsStream(getTasksXmlPath());
	}

	@Override
	protected Logger getLOG() {
		return LOG;
	}

	@Override
	protected String getModule() {
		return CoreModuleDescriptor.MODULE_ID;
	}

	@Override
	protected String getTasksXmlPath() {
		return DEFAULT_RESOURCE + CORE_SCHEDULED_TASK_XML;
	}

}
