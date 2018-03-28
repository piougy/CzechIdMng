package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * Test task executor implementation.
 * Publish notify event on identities.
 * 
 * @author Radek Tomiška
 *
 */
@Service
@DisallowConcurrentExecution
@Description("Publish notify event on identities")
@ConditionalOnProperty(prefix = "idm.pub.app", name = "stage", havingValue = "development")
public class NotifyIdentityTaskExecutor extends AbstractSchedulableStatefulExecutor<IdmIdentityDto> {
	
	private static final String PARAMETER_TEXT = "text";
	//
	@Autowired private IdmIdentityService identityService;
	@Autowired private EntityEventManager entityEventManager;
	//
	private String text;
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		text = getParameterConverter().toString(properties, PARAMETER_TEXT);
	}
	
	@Override
	public Page<IdmIdentityDto> getItemsToProcess(Pageable pageable) {
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setText(text);
		//
		return identityService.find(filter, pageable);
	}

	@Override
	public Optional<OperationResult> processItem(IdmIdentityDto dto) {
		try {
			entityEventManager.changedEntity(dto);
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		} catch (Exception ex) {
			throw new CoreException(ex);
		}
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_TEXT);
		return parameters;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = super.getProperties();
		properties.put(PARAMETER_TEXT, text);
		return properties;
	}
}
