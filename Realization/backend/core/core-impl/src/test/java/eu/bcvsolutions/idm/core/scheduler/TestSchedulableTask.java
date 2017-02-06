package eu.bcvsolutions.idm.core.scheduler;

import java.util.Map;

import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableTaskExecutor;

/**
 * Test task for {@link DefaultSchedulerManagerIntegrationTest}.
 * 
 * @author Radek Tomiška
 *
 */
public class TestSchedulableTask extends AbstractSchedulableTaskExecutor {

	private String result;

	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		result = (String) properties.get(DefaultSchedulerManagerIntegrationTest.RESULT_PROPERTY);
	}

	@Override
	public void process() {
		DefaultSchedulerManagerIntegrationTest.processed = result;
	}

}
