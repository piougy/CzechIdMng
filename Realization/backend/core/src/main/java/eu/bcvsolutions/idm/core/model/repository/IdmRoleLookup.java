package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.domain.IdentifiableByNameLookup;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

@Component
public class IdmRoleLookup extends IdentifiableByNameLookup<IdmRole>{

	@Autowired 
	private ApplicationContext applicationContext;
	
	@Autowired
	private IdmRoleRepository roleRepository;
	
	@Override
	public IdmRole findOneByName(String name) {
		return getRepository().findOneByName(name);
	}

	@Override
	public IdmRole findOne(Long id) {
		return getRepository().findOne(id);
	}

	/**
	 * We need to inject repository lazily - we need security AOP to take effect
	 * 
	 * @return
	 */
	private IdmRoleRepository getRepository() {
		if (roleRepository == null) {
			roleRepository = applicationContext.getBean(IdmRoleRepository.class);
		}
		return roleRepository;
	}
	

}
