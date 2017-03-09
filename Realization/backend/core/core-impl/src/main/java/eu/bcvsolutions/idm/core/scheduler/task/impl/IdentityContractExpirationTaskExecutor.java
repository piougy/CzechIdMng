package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.Map;

import org.joda.time.LocalDate;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableTaskExecutor;

/**
 * Delete expired identity contracts
 * 
 * @author Radek Tomiška
 *
 */
@Service
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Description("Delete expired identity contracts")
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
		LOG.debug("Delete expired identity contracts was inintialized for expiration less than [{}]", expiration);
	}
	
	@Override
	public Boolean process() {
		LOG.info("Delete expired identity contracts starts for expiration less than [{}]", expiration);
		counter = 0L;
		boolean canContinue = true;
		while(canContinue) {
			// we process all expired contract
			Page<IdmIdentityContract> expiredContracts = identityContractService.findExpiredContracts(expiration, new PageRequest(0, 100));
			// init count
			if (count == null) {
				count = expiredContracts.getTotalElements();
			}
			//
			for(IdmIdentityContract contract : expiredContracts) {
				identityContractService.delete(contract);
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
		LOG.info("Delete expired identity contracts ended for expiration less than [{}]", expiration);
		return Boolean.TRUE;
	}
}
