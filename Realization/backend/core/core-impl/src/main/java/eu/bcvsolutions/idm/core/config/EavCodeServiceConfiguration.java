package eu.bcvsolutions.idm.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.filter.EavCodeManagersByContractFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.EavCodeManagersFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.EavCodeSubordinatesFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.ManagersByContractFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.ManagersFilter;
import eu.bcvsolutions.idm.core.model.repository.filter.SubordinatesFilter;

/**
 * TODO: move to custom module, if filter registration mechanism will not be available.
 * 
 * @author Radek Tomiška
 *
 */
@Order(-1)
@Configuration
public class EavCodeServiceConfiguration {
	
	@Autowired private IdmIdentityRepository identityRepository;

	/**
	 * Overriden Subordinates criteria builder.
	 * 
	 * @return
	 */
	@Bean
	public SubordinatesFilter subordinatesFilter() {
		return new EavCodeSubordinatesFilter(identityRepository);
	}
	
	/**
	 * Overriden managers criteria builder.
	 * 
	 * @return
	 */
	@Bean
	public ManagersFilter managersFilter() {
		return new EavCodeManagersFilter(identityRepository);
	}
	
	/**
	 * Overriden managers criteria builder (by contract id).
	 * 
	 * @return
	 */
	@Bean
	public ManagersByContractFilter managersByContractFilter() {
		return new EavCodeManagersByContractFilter(identityRepository);
	}
}
