package eu.bcvsolutions.idm.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.filter.DefaultManagersByContractFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.DefaultManagersFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.DefaultSubordinatesFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.ManagersByContractFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.ManagersFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.SubordinatesFilter;

/**
 * TODO: registrable
 * 
 * @author Radek Tomiška
 *
 */
@Order(0)
@Configuration
public class IdmFilterConfiguration {
	
	@Autowired private IdmIdentityRepository identityRepository;

	/**
	 * Subordinates criteria builder.
	 * 
	 * Override in custom module for changing subordinates evaluation.
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(SubordinatesFilter.class)
	public SubordinatesFilter subordinatesFilter() {
		return new DefaultSubordinatesFilter(identityRepository);
	}
	
	/**
	 * Managers criteria builder.
	 * 
	 * Override in custom module for changing managers evaluation.
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(ManagersFilter.class)
	public ManagersFilter managersFilter() {
		return new DefaultManagersFilter(identityRepository);
	}
	
	/**
	 * Managers criteria builder (by contract id).
	 * 
	 * Override in custom module for changing managers evaluation.
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean(ManagersByContractFilter.class)
	public ManagersByContractFilter managersByContractFilter() {
		return new DefaultManagersByContractFilter(identityRepository);
	}
}
