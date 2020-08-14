package eu.bcvsolutions.idm.core.scheduler.config;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Abstract class for initial default long running task.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @deprecated @since 10.5.0 use BaseScheduledTaskInitializer with event processor.
 */
@Deprecated
public abstract class AbstractScheduledTaskInitializer 
		extends BaseScheduledTaskInitializer 
		implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// init default long running task
		initScheduledTask(getTasksInputStream());
	}
}
