package eu.bcvsolutions.idm.core.rest.lookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.rest.lookup.CodeableDtoLookup;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;

/**
 * Role dto lookup (codeable)
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class IdmRoleDtoLookup extends CodeableDtoLookup<IdmRoleDto>{

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
