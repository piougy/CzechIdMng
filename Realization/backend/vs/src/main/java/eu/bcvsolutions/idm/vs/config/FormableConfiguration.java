package eu.bcvsolutions.idm.vs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormValueService;
import eu.bcvsolutions.idm.vs.entity.VsAccount;
import eu.bcvsolutions.idm.vs.entity.VsAccountFormValue;

/**
 * Configuration for eav
 * 
 * @author Svanda
 *
 */
@Configuration(value="vsFormableConfiguration")
public class FormableConfiguration {

	/**
	 * Eav attributes for vs account
	 * 
	 * @param repository
	 * @param confidentialStorage
	 * @return
	 */
	@Bean
	public AbstractFormValueService<VsAccount, VsAccountFormValue> vsAccountFormValueService(
			AbstractFormValueRepository<VsAccount, VsAccountFormValue> repository, 
			ConfidentialStorage confidentialStorage) {
		return new AbstractFormValueService<VsAccount, VsAccountFormValue>(repository, confidentialStorage) {};
	}
	
}
