package eu.bcvsolutions.idm.core.rest.processor;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * Secure confidential properties
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class IdmConfigurationProcessor implements ResourceProcessor<Resource<IdmConfiguration>> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmConfigurationProcessor.class);
	
	@Override
	public Resource<IdmConfiguration> process(Resource<IdmConfiguration> resource) {
		IdmConfiguration configuration = resource.getContent();
		//
		// password etc. has to be guarded - can be used just in BE
		if(GuardedString.shouldBeGuarded(configuration.getName())) {
			LOG.debug("Configuration value for property [{}] is guarded.", configuration.getName());
			configuration.setValue(GuardedString.SECRED_PROXY_STRING);
		}
		return resource;
	}
}
