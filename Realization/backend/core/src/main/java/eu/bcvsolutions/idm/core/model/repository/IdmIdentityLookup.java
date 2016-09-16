package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.domain.IdentifiableByNameLookup;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

@Component
public class IdmIdentityLookup extends IdentifiableByNameLookup<IdmIdentity> {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private IdmIdentityRepository identityRepository;

	@Override
	public IdmIdentity findOneByName(String name) {
		return getRepository().findOneByUsername(name);
	}

	@Override
	public IdmIdentity findOne(Long id) {
		return getRepository().findOne(id);
	}

	/**
	 * We need to inject repository lazily - we need security AOP to take effect
	 * 
	 * @return
	 */
	private IdmIdentityRepository getRepository() {
		if (identityRepository == null) {
			identityRepository = applicationContext.getBean(IdmIdentityRepository.class);
		}
		return identityRepository;
	}
}
