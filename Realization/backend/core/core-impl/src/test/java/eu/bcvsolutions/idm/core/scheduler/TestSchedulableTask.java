package eu.bcvsolutions.idm.core.scheduler;

import java.util.Map;

import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableTaskExecutor;

/**
 * Test task for {@link DefaultSchedulerManagerIntegrationTest}.
 * 
 * @author Radek Tomiška
 *
 */
public class TestSchedulableTask extends AbstractSchedulableTaskExecutor<String> {

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
		result = (String) properties.get(DefaultSchedulerManagerIntegrationTest.RESULT_PROPERTY);
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
}
