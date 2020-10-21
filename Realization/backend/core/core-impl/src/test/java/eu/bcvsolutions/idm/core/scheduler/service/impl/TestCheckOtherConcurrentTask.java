package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.scheduler.ObserveLongRunningTaskEndProcessor;
import eu.bcvsolutions.idm.core.scheduler.api.domain.IdmCheckConcurrentExecution;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Test task for {@link DefaultSchedulerManagerIntegrationTest}.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@IdmCheckConcurrentExecution(taskTypes = { TestCheckConcurrentTaskTwo.class })
public class TestCheckOtherConcurrentTask extends AbstractSchedulableTaskExecutor<String> {

	private String result;
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		result = (String) properties.get(ObserveLongRunningTaskEndProcessor.RESULT_PROPERTY);
	}
	
	@Override
	public String process() {
		counter = 0L;
		for (long i = 0; i < 2; i++) {
			counter++;
			if (!updateState()) {
				break;
			}
		}			
		return result;
	}
	
	@Override
	public boolean isDisabled() {
		return false;
	}
}
