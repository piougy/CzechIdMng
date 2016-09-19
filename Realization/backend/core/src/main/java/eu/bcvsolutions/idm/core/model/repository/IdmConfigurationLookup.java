package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.domain.IdentifiableByNameLookup;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;

@Component
public class IdmConfigurationLookup extends IdentifiableByNameLookup<IdmConfiguration> {
	
	@Autowired 
	private ApplicationContext applicationContext;
	
	private IdmConfigurationRepository configurationRepository;

	@Override
	public IdmConfiguration findOneByName(String name) {
		return getRepository().findOneByName(name);
	}

	@Override
	public IdmConfiguration findOne(Long id) {
		return getRepository().findOne(id);
	}
	
	/**
	 * We need to inject repository lazily - we need security AOP to take effect
	 *   
	 * @return
	 */
	private IdmConfigurationRepository getRepository() {
		if (configurationRepository == null) { 
			configurationRepository = applicationContext.getBean(IdmConfigurationRepository.class);
		}
		return configurationRepository;
	}
}
