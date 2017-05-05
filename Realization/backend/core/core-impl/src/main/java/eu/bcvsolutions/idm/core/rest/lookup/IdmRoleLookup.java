package eu.bcvsolutions.idm.core.rest.lookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.rest.lookup.CodeableServiceEntityLookup;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;

@Component
public class IdmRoleLookup extends CodeableServiceEntityLookup<IdmRole>{

	@Autowired 
	private ApplicationContext applicationContext;
	
	private IdmRoleService roleService;

	/**
	 * We need to inject repository lazily - we need security AOP to take effect
	 * 
	 * @return
	 */
	@Override
	protected IdmRoleService getService() {
		if (roleService == null) {
			roleService = applicationContext.getBean(IdmRoleService.class);
		}
		return roleService;
	}
	

}
