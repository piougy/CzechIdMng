package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.scheduler.ObserveLongRunningTaskEndProcessor;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Test task for {@link DefaultSchedulerManagerIntegrationTest}. Updates identity lastName = username.
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class TestUpdateIdentityTask extends AbstractSchedulableTaskExecutor<String> {
	
	private String result;
	//
	@Autowired private IdmIdentityService identityService;
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		result = (String) properties.get(ObserveLongRunningTaskEndProcessor.RESULT_PROPERTY);
	}
	
	@Override
	public String process() {
		IdmIdentityDto identity = identityService.getByUsername(result);
		identity.setLastName(result);
		identityService.save(identity);
		//
		return result;
	}
}
