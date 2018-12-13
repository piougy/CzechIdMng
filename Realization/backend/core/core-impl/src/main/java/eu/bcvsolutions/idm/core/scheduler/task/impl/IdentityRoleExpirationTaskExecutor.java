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
 * TODO: statefull + continue on exception
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
			Page<IdmIdentityRoleDto> assignedRoles = service.findExpiredRoles(expiration, new PageRequest(0, pageSize)); // 0 => from start - roles from previous search are already removed
			hasNextPage = assignedRoles.hasContent();
			if (count == null) {
				count = assignedRoles.getTotalElements();
			}
			
			for (Iterator<IdmIdentityRoleDto> i = assignedRoles.iterator(); i.hasNext() && hasNextPage;) {
				IdmIdentityRoleDto assignedRole = i.next();
				
				if (assignedRole.getDirectRole() == null) { // sub role will be removed by it's direct role
					LOG.debug("Remove role: [{}] from contract id: [{}].", assignedRole.getRole(), assignedRole.getIdentityContract());
					service.delete(assignedRole);
				}
				++counter;
				hasNextPage &= updateState();
			}
		} while (hasNextPage);
		
		LOG.info("Expired roles removal task ended. Removed roles: [{}].", counter);
		
		return Boolean.TRUE;
	}
}
