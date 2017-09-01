package eu.bcvsolutions.idm.core.rest.lookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.rest.lookup.CodeableDtoLookup;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;

/**
 * Configuration item dto lookup
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class IdmConfigurationDtoLookup extends CodeableDtoLookup<IdmConfigurationDto>{

	@Autowired 
	private ApplicationContext applicationContext;
	
	private IdmConfigurationService service;

	/**
	 * We need to inject repository lazily - we need security AOP to take effect
	 * 
	 * @return
	 */
	@Override
	protected IdmConfigurationService getService() {
		if (service == null) {
			service = applicationContext.getBean(IdmConfigurationService.class);
		}
		return service;
	}
}
