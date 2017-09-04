package eu.bcvsolutions.idm.acc.rest.lookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.rest.lookup.CodeableDtoLookup;

@Component
public class SysSystemLookup extends CodeableDtoLookup<SysSystemDto> {
	
	@Autowired 
	private ApplicationContext applicationContext;
	
	private SysSystemService systemService;
	
	/**
	 * We need to inject repository lazily - we need security AOP to take effect
	 *   
	 * @return
	 */
	@Override
	protected SysSystemService getService() {
		if (systemService == null) { 
			systemService = applicationContext.getBean(SysSystemService.class);
		}
		return systemService;
	}
}
