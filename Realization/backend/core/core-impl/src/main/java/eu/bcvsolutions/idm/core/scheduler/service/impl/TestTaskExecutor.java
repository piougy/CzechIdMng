package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.exception.CoreException;

/**
 * Test task executor implementation
 * 
 * @author Radek Tomiška
 *
 */
@Service
@Description("Test long running task")
public class TestTaskExecutor extends AbstractSchedulableTaskExecutor {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TestTaskExecutor.class);
	private static final String PARAMETER_COUNT = "count";
	private Long count;
	private Long counter;
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		count = getParameterConverter().toLong(properties, PARAMETER_COUNT);
		if (count == null) {
			count = 100L;
		}
		counter = 0L;
	}
	
	@Override
	public void process() {
		// 
		try {
			for (long i = 1; i <= getCount(); i++) {
				LOG.warn(".......... Counter: " + i);
				counter = i;
				if (!updateState()) {
					break;
				}
				Thread.sleep(300L);
			}
		} catch (Exception ex) {
			throw new CoreException(ex);
		}
	}
	
	@Override
	public Long getCount() {
		return count;
	}
	
	@Override
	public Long getCounter() {
		return counter;
	}
	
	public void setCount(Long count) {
		this.count = count;
	}
	
	@Override
	public List<String> getParameterNames() {
		List<String> parameters = super.getParameterNames();
		parameters.add(PARAMETER_COUNT);
		return parameters;
	}
}
