package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.Map;
import java.util.Optional;

import org.joda.time.LocalDate;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableStatefulExecutor;

/**
 * Remove roles by expired identity contracts (=> removes assigned roles).
 * 
 * @author Radek Tomiška
 *
 */
@Service
@DisallowConcurrentExecution
@Description("Remove roles by expired identity contracts (=> removes assigned roles).")
public class IdentityContractExpirationTaskExecutor extends AbstractSchedulableStatefulExecutor<IdmIdentityContractDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityContractExpirationTaskExecutor.class);
	//
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	//
	private LocalDate expiration;
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		expiration = new LocalDate();
		LOG.debug("Remove roles  expired identity contracts was inintialized for expiration less than [{}]", expiration);
	}

	@Override
	public Page<IdmIdentityContractDto> getItemsToProcess(Pageable pageable) {
		return identityContractService.findExpiredContracts(expiration, pageable);
	}

	@Override
	public Optional<OperationResult> processItem(IdmIdentityContractDto dto) {
		LOG.info("Remove roles by expired identity contract [{}]. Contract ended for expiration less than [{}]",  dto.getId(), expiration);
		try {
			// remove all referenced roles
			identityRoleService.findAllByContract(dto.getId()).forEach(identityRole -> {
				identityRoleService.delete(identityRole);
			});
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		} catch (Exception ex) {
			LOG.error("Removing roles of expired contract [{}] failed", dto.getId(), ex);
			return Optional.of(new OperationResult.Builder(OperationState.EXCEPTION)
					.setCause(ex)
					.build());
		}
	}
}
