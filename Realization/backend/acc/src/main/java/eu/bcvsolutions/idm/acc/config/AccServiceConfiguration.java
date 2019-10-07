package eu.bcvsolutions.idm.acc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemFormValue;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningAttributeRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemFormValueService;
import eu.bcvsolutions.idm.acc.service.impl.DefaultSysProvisioningAttributeService;
import eu.bcvsolutions.idm.acc.service.impl.DefaultSysSystemFormValueService;
import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;

/**
 * Module services configuration
 * 
 * @author Radek Tomiška
 *
 */
@Order(0)
@Configuration
public class AccServiceConfiguration {
	
	//
	// Spring Data repositories through interfaces - they are constructed automatically
	@Autowired private SysProvisioningAttributeRepository provisioningAttributeRepository;

	/**
	 * Eav attributes for target system configuration
	 * 
	 * @param repository
	 * @param confidentialStorage
	 * @return
	 */
	@Bean
	public SysSystemFormValueService systemFormValueService(AbstractFormValueRepository<SysSystem, SysSystemFormValue> repository) {
		return new DefaultSysSystemFormValueService(repository);
	}
	
	/**
	 * Service for logging schema attributes used in provisioning archive or operation (connector attributes).
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(SysProvisioningAttributeService.class)
	public SysProvisioningAttributeService provisioningAttributeService() {
		return new DefaultSysProvisioningAttributeService(provisioningAttributeRepository);
	}
}
