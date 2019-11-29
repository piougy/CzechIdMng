package eu.bcvsolutions.idm.tool;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Initialize Tool module
 *
 * @author BCV solutions s.r.o.
 *
 */
@Component
@DependsOn("initApplicationData")
public class ToolModuleInitializer implements ApplicationListener<ContextRefreshedEvent> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ToolModuleInitializer.class);

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		LOG.info("Module [{}] initialization", ToolModuleDescriptor.MODULE_ID);
	}
}
