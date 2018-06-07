package eu.bcvsolutions.idm.core.bulk.action.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.bulk.action.IdmBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractLongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Abstract parent for all bulk actions
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 * @param <DTO>
 */
public abstract class AbstractBulkAction<DTO extends BaseDto>
		extends AbstractLongRunningTaskExecutor<OperationResult>
		implements IdmBulkAction<DTO> {

	private IdmBulkActionDto action;
	@Autowired
	private NotificationManager notificationManager;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private ConfigurationService configurationService;
	
	@Override
	public IdmBulkActionDto getAction() {
		return action;
	}

	@Override
	public void setAction(IdmBulkActionDto action) {
		this.action = action;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		return new ArrayList<>();
	}
	
	@Override
	public void validate() {
		Assert.notNull(action, "Action can't be null");
		//
		if (action.getIdentifiers() == null && action.getFilter() == null) {
			throw new ResultCodeException(CoreResultCode.BULK_ACTION_ENTITIES_ARE_NOT_SPECIFIED);
		}
		//
		if (action.getIdentifiers() != null && action.getFilter() != null) {
			throw new ResultCodeException(CoreResultCode.BULK_ACTION_ONLY_ONE_FILTER_CAN_BE_APPLIED);
		}
		//
		for (IdmFormAttributeDto attribute : this.getFormAttributes()) {
			if (attribute.isRequired()) {
				if (action.getProperties() == null) {
					// this state is also possible
					throw new ResultCodeException(CoreResultCode.BULK_ACTION_REQUIRED_PROPERTY, ImmutableMap.of("attributeCode", attribute.getCode()));
				}
				Object value = action.getProperties().get(attribute.getCode());
				if (value == null) {
					throw new ResultCodeException(CoreResultCode.BULK_ACTION_REQUIRED_PROPERTY, ImmutableMap.of("attributeCode", attribute.getCode()));
				} 
				if (attribute.isMultiple()) {
					Collection<?> multivaluedValue= (Collection<?>)value;
					if (multivaluedValue.isEmpty()) {
						throw new ResultCodeException(CoreResultCode.BULK_ACTION_REQUIRED_PROPERTY, ImmutableMap.of("attributeCode", attribute.getCode()));
					}
				}
			}
		}
	}
	
	@Override
	public Map<String, Object> getProperties() {
		if (this.getAction() == null) {
			return super.getProperties();
		}
		if (this.getAction().getProperties() == null) {
			return super.getProperties();
		}
		return this.getAction().getProperties();
	}
	
	@Override
	public Map<String, BasePermission[]> getPermissions() {
		return new HashMap<>();
	}
	
	@Override
	public int getOrder() {
		return DEFAULT_ORDER;
	}
	
	@Override
	protected OperationResult end(OperationResult result, Exception ex) {
		OperationResult end = null;
		if (result != null && result.getException() != null) {
			end = super.end(result, (Exception) result.getException());
		}
		end = super.end(result, ex);
		// send message
		IdmLongRunningTaskDto task = getLongRunningTaskService().get(getLongRunningTaskId());
		IdmBulkActionDto action = getAction();
		//
		IdmIdentityDto identityDto = identityService.get(task.getCreatorId());
		if (identityDto != null) {
			notificationManager.send(CoreModuleDescriptor.TOPIC_BULK_ACTION_END,
					new IdmMessageDto.Builder()
					.addParameter("action", action)
					.addParameter("task", task)
					.addParameter("owner", identityDto)
					.addParameter("result", end)
					.addParameter("detailUrl", configurationService.getFrontendUrl(String.format("scheduler/all-tasks/%s/detail", task.getId())))
					.addParameter("processItemslUrl", configurationService.getFrontendUrl(String.format("scheduler/all-tasks/%s/items", task.getId())))
					.build(),
					identityDto);
		}
		//
		return end;
	}
}
