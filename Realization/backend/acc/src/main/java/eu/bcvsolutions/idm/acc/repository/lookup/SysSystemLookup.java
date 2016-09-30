package eu.bcvsolutions.idm.acc.repository.lookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.service.SysSystemService;
import eu.bcvsolutions.idm.core.rest.domain.IdentifiableByNameLookup;

@Component
public class SysSystemLookup extends IdentifiableByNameLookup<SysSystem> {
	
	@Autowired 
	private ApplicationContext applicationContext;
	
	private SysSystemService systemService;
	
	/**
	 * We need to inject repository lazily - we need security AOP to take effect
	 *   
	 * @return
	 */
	@Override
	protected SysSystemService getEntityService() {
		if (systemService == null) { 
			systemService = applicationContext.getBean(SysSystemService.class);
		}
		return systemService;
	}
}
