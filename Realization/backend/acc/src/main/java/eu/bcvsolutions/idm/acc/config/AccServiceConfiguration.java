package eu.bcvsolutions.idm.acc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemFormValue;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormValueService;

/**
 * Module services configuration
 * 
 * @author Radek Tomiška
 *
 */
@Order(0)
@Configuration
public class AccServiceConfiguration {

	/**
	 * Eav attributes for target system configuration
	 * 
	 * @param repository
	 * @param confidentialStorage
	 * @return
	 */
	@Bean
	public AbstractFormValueService<SysSystem, SysSystemFormValue> systemFormValueService(
			AbstractFormValueRepository<SysSystem, SysSystemFormValue> repository, 
			ConfidentialStorage confidentialStorage) {
		return new AbstractFormValueService<SysSystem, SysSystemFormValue>(repository, confidentialStorage) {};
	}
}
