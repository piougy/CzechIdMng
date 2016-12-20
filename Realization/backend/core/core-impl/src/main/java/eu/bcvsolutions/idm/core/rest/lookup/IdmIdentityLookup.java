package eu.bcvsolutions.idm.core.rest.lookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.rest.lookup.IdentifiableByNameLookup;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;

@Component
public class IdmIdentityLookup extends IdentifiableByNameLookup<IdmIdentity> {

	@Autowired
	private ApplicationContext applicationContext;
	
	private IdmIdentityService identityService;

	/**
	 * We need to inject service lazily - we need security AOP to take effect
	 * 
	 * @return
	 */
	protected IdmIdentityService getEntityService() {
		if (identityService == null) {
			identityService = applicationContext.getBean(IdmIdentityService.class);
		}
		return identityService;
	}
}
