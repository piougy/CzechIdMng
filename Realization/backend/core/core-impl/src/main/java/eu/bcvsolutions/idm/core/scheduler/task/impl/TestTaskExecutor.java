package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableStatefulExecutor;

/**
 * Test task executor implementation
 * 
 * @author Radek Tomiška
 *
 */
@Service
@Description("Test long running task")
@ConditionalOnProperty(prefix = "idm.pub.app", name = "stage", havingValue = "development")
public class TestTaskExecutor extends AbstractSchedulableStatefulExecutor<IdmIdentityDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TestTaskExecutor.class);
	private static final String PARAMETER_COUNT = "count";
	
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
	public Page<IdmIdentityDto> getItemsToProcess(Pageable pageable) {
		List<IdmIdentityDto> identities = new ArrayList<>();
		for (int i = 0; i < count; i ++) {
			identities.add(new IdmIdentityDto(UUID.randomUUID(), "test-" + i));
		}
		return new PageImpl<>(identities);
	}

	@Override
	public Optional<OperationResult> processItem(IdmIdentityDto dto) {
		try {
			LOG.warn(".......... identity: [{}]", dto.getUsername());
			Thread.sleep(300L);
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		} catch (Exception ex) {
			throw new CoreException(ex);
		}
	}
	
	@Override
	public List<String> getParameterNames() {
		List<String> parameters = super.getParameterNames();
		parameters.add(PARAMETER_COUNT);
		return parameters;
	}
}
