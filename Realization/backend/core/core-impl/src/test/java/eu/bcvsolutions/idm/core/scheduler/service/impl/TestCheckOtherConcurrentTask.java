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
@IdmCheckConcurrentExecution(taskTypes = { TestCheckConcurrentTask.class })
public class TestCheckOtherConcurrentTask extends AbstractSchedulableTaskExecutor<String> {

	private String result;
	private Long count;
	private Long counter;
	
	@Override
	public Long getCount() {
		return count;
	}
	@Override
	public Long getCounter() {
		return counter;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		count = 5L;
		counter = 0L;
		result = (String) properties.get(ObserveLongRunningTaskEndProcessor.RESULT_PROPERTY);
	}
	
	@Override
	public String process() {
		for (long i = 0; i < count; i++) {
			counter++;
			if(!updateState()) {
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
