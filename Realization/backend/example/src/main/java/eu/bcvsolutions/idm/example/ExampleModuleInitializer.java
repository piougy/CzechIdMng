package eu.bcvsolutions.idm.example;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Initialize example module
 * 
 * @author Radek Tomiška
 *
 */
@Component
@DependsOn("initApplicationData")
public class ExampleModuleInitializer implements ApplicationListener<ContextRefreshedEvent> {
 
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExampleModuleInitializer.class);
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		LOG.info("Module [{}] initialization", ExampleModuleDescriptor.MODULE_ID);
	}
}