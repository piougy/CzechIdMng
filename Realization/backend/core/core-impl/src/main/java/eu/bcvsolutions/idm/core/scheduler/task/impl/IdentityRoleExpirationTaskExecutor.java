package eu.bcvsolutions.idm.core.scheduler.task.impl;

import org.joda.time.LocalDate;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableTaskExecutor;

/**
 * Long running task for expired identity roles removal.
 * Expected usage is in cooperation with CronTaskTrigger, running
 * once a day after midnight.
 * 
 */
@Service
@DisallowConcurrentExecution
@Description("Removes expired roles from identites.")
public class IdentityRoleExpirationTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {
	
	private static final Logger LOG = LoggerFactory.getLogger(IdentityRoleExpirationTaskExecutor.class);
	
	private LocalDate expiration;
	
	@Autowired
	private IdmIdentityRoleService service;
	
	@Override
	public Boolean process() {
		this.expiration = new LocalDate();
		this.counter = 0L;
		
		LOG.debug("Expired roles removal task started with limit date [{}].", expiration);

		int page = 0;
		int pageSize = 100;
		boolean hasNextPage = false;
		try {
			do {
				Page<IdmIdentityRole> roles = getPagedRoles(page, pageSize);
				hasNextPage = roles.hasContent();
				if (count == null) {
					count = roles.getTotalElements();
				}
				
				for (IdmIdentityRole role : roles) {
					service.delete(role);
					++counter;
					hasNextPage &= updateState();
				}
				
				++page;
			} while (hasNextPage);
		} catch (Exception e) {
			LOG.error("An error occurred while removing expired roles.", e);
			return Boolean.FALSE;
		}
		
		return Boolean.TRUE;
	}

	private Page<IdmIdentityRole> getPagedRoles(int page, int pageSize) {
		return service.findExpiredRoles(expiration, new PageRequest(page, pageSize));
	}

}
