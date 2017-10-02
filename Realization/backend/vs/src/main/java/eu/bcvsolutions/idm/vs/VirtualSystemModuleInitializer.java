package eu.bcvsolutions.idm.vs;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Initialize Virtual system module
 * 
 * @author Svanda
 *
 */
@Component
@DependsOn("initApplicationData")
public class VirtualSystemModuleInitializer implements ApplicationListener<ContextRefreshedEvent> {
 
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(VirtualSystemModuleInitializer.class);
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		LOG.info("Module [{}] initialization", VirtualSystemModuleDescriptor.MODULE_ID);
	}
}