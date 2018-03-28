package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.Iterator;
import java.util.Map;

import org.joda.time.LocalDate;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Long running task for expired identity roles removal.
 * Expected usage is in cooperation with CronTaskTrigger, running
 * once a day after midnight.
 * 
 * @author Jan Helbich
 * @author Radek Tomiška
 */
@Service
@DisallowConcurrentExecution
@Description("Removes expired roles from identites.")
public class IdentityRoleExpirationTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {
	
	private static final Logger LOG = LoggerFactory.getLogger(IdentityRoleExpirationTaskExecutor.class);
	//
	@Autowired private IdmIdentityRoleService service;
	//
	private LocalDate expiration;
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		expiration = new LocalDate();
		LOG.info("Expired roles removal task was inintialized for expiration less than [{}].", expiration);
	}
	
	@Override
	public Boolean process() {
		this.counter = 0L;
		//
		int pageSize = 100;
		boolean hasNextPage = false;
		do {
			Page<IdmIdentityRoleDto> roles = service.findExpiredRoles(expiration, new PageRequest(0, pageSize)); // 0 => from start - roles from previous search are already removed
			hasNextPage = roles.hasContent();
			if (count == null) {
				count = roles.getTotalElements();
			}
			
			for (Iterator<IdmIdentityRoleDto> i = roles.iterator(); i.hasNext() && hasNextPage;) {
				IdmIdentityRoleDto role = i.next();
				
				LOG.debug("Remove role: [{}] from contract id: [{}].", role.getRole(), role.getIdentityContract());
				
				service.delete(role);
				++counter;
				hasNextPage &= updateState();
			}
		} while (hasNextPage);
		
		LOG.info("Expired roles removal task ended. Removed roles: [{}].", counter);
		
		return Boolean.TRUE;
	}
}
