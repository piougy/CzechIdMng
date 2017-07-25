package eu.bcvsolutions.idm.example;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.PropertyModuleDescriptor;

/**
 * Example module descriptor
 * 
 * @author Radek Tomiška
 *
 */
@Component
@PropertySource("classpath:module-" + ExampleModuleDescriptor.MODULE_ID + ".properties")
@ConfigurationProperties(prefix = "module." + ExampleModuleDescriptor.MODULE_ID + ".build", ignoreUnknownFields = true, ignoreInvalidFields = true)
public class ExampleModuleDescriptor extends PropertyModuleDescriptor {

	public static final String MODULE_ID = "example";
	
	@Override
	public String getId() {
		return MODULE_ID;
	}
	
	@Override
	public boolean isDocumentationAvailable() {
		return true;
	}
}
