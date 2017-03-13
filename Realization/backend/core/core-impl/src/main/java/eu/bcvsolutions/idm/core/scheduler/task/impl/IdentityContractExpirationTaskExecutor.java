package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.Map;

import org.joda.time.LocalDate;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableTaskExecutor;

/**
 * Disables expired identity contracts (=> removes assigned roles).
 * 
 * @author Radek Tomiška
 *
 */
@Service
@DisallowConcurrentExecution
@Description("Disables expired identity contracts (=> removes assigned roles).")
public class IdentityContractExpirationTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityContractExpirationTaskExecutor.class);
	@Autowired 
	private IdmIdentityContractService identityContractService;
	//
	private LocalDate expiration;	
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		expiration = new LocalDate();
		LOG.debug("Disable expired identity contracts was inintialized for expiration less than [{}]", expiration);
	}
	
	@Override
	public Boolean process() {
		LOG.info("Disable expired identity contracts starts for expiration less than [{}]", expiration);
		counter = 0L;
		boolean canContinue = true;
		while(canContinue) {
			// we process all expired contract
			Page<IdmIdentityContract> expiredContracts = identityContractService.findExpiredContracts(expiration, false, new PageRequest(0, 100));
			// init count
			if (count == null) {
				count = expiredContracts.getTotalElements();
			}
			//
			for(IdmIdentityContract contract : expiredContracts) {
				contract.setDisabled(true);
				identityContractService.save(contract);
				counter++;
				canContinue = updateState();
				if (!canContinue) {
					break;
				}
			}
			if (!expiredContracts.hasNext()) {
				break;
			}
		}
		LOG.info("Disable expired identity contracts ended for expiration less than [{}]", expiration);
		return Boolean.TRUE;
	}
}
