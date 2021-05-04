package eu.bcvsolutions.idm.core.bulk.action.impl.configuration;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Switch instance for asynchronous processing:
 * - scheduled tasks will be processed on new instance
 * - asynchronous events will be processed on new instance
 * - all created long running tasks and events will be moved to new instance.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(ConfigurationSwitchInstanceBulkAction.NAME)
@Description("Switch instance for asynchronous processing.")
public class ConfigurationSwitchInstanceBulkAction extends AbstractBulkAction<IdmConfigurationDto, DataFilter> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigurationSwitchInstanceBulkAction.class);
	//
	public static final String NAME = "core-configuration-switch-instance-bulk-action";
	public static final String PROPERTY_PREVIOUS_INSTANCE_ID = "previous-instance-id";
	public static final String PROPERTY_NEW_INSTANCE_ID = "new-instance-id";
	//
	@Autowired private IdmConfigurationService configurationService;
	@Autowired private EventConfiguration eventConfiguration;
	@Autowired private SchedulerManager schedulerManager;
	@Autowired private EntityEventManager eventManager;
	@Autowired private LongRunningTaskManager taskManager;
	@Autowired private SecurityService securityService;
	//
	private boolean propertyCreated = false;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public List<String> getAuthorities() {
		List<String> authorities = super.getAuthorities(); // adds CoreGroupPermission.CONFIGURATION_UPDATE bellow
		// 
		authorities.add(CoreGroupPermission.SCHEDULER_EXECUTE);
		authorities.add(CoreGroupPermission.SCHEDULER_UPDATE);
		authorities.add(CoreGroupPermission.ENTITYEVENT_UPDATE);
		//
		return authorities;
	}
	
	@Override
	protected List<String> getAuthoritiesForEntity() {
		List<String> authorities =  super.getAuthoritiesForEntity();
		// configuration update is needed to check on item only
		authorities.add(CoreGroupPermission.CONFIGURATION_UPDATE);
		//
		return authorities;
	}
	
	@Override
	public ReadWriteDtoService<IdmConfigurationDto, DataFilter> getService() {
		return configurationService;
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 15000;
	}
	
	@Override
	public boolean showWithoutSelection() {
		return true;
	}

	@Override
	public boolean showWithSelection() {
		return false;
	}
	
	@Override
	public boolean isQuickButtonable() {
		return false;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		String currentInstanceId = configurationService.getInstanceId();
		//
		IdmFormAttributeDto previousInstanceId = new IdmFormAttributeDto(
				PROPERTY_PREVIOUS_INSTANCE_ID, PROPERTY_PREVIOUS_INSTANCE_ID, PersistentType.SHORTTEXT
		);
		previousInstanceId.setRequired(true);
		//
		Set<String> previousInstanceIds = getPreviousInstanceIds(currentInstanceId);
		if (!previousInstanceIds.isEmpty()) {
			previousInstanceId.setDefaultValue(previousInstanceIds.iterator().next());
		}
		//
		IdmFormAttributeDto newInstanceId = new IdmFormAttributeDto(
				PROPERTY_NEW_INSTANCE_ID, PROPERTY_NEW_INSTANCE_ID, PersistentType.SHORTTEXT
		);
		newInstanceId.setRequired(true);
		newInstanceId.setDefaultValue(currentInstanceId);
		//
		formAttributes.add(previousInstanceId);
		formAttributes.add(newInstanceId);
		//
		return formAttributes;
	}
	
	@Override
	protected List<UUID> getAllEntities(IdmBulkActionDto action, StringBuilder description) {
		// based on asynchronous event processing
		IdmConfigurationDto instanceId = configurationService.getByCode(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_INSTANCE_ID);
		if (instanceId != null) {
			return Lists.newArrayList(instanceId.getId());
		}
		//
		configurationService.setValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_INSTANCE_ID, eventConfiguration.getAsynchronousInstanceId());
		propertyCreated = true;
		//
		return Lists.newArrayList(configurationService.getByCode(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_INSTANCE_ID).getId());
	}
	
	@Override
	protected OperationResult processDto(IdmConfigurationDto dto) {
		List<String> authorities = getAuthorities();
		if (!securityService.hasAllAuthorities(authorities.toArray(new String[authorities.size()]))) {
			return new OperationResult
					.Builder(OperationState.NOT_EXECUTED)
					.setModel(
							new DefaultResultModel(
									CoreResultCode.BULK_ACTION_INSUFFICIENT_PERMISSION,
									ImmutableMap.of(
											"bulkAction", this.getAction().getName(),
											"entityId", dto.getId(),
											"entityCode", dto.getCode()
									)
							)
					)
					.build();
		}
		//
		String previousInstanceId = getParameterConverter().toString(getProperties(), PROPERTY_PREVIOUS_INSTANCE_ID);
		String newInstanceId = getParameterConverter().toString(getProperties(), PROPERTY_NEW_INSTANCE_ID);
		//
		try {
			if (previousInstanceId.equals(newInstanceId)) {
				LOG.info("Previous instance is the same as newly used for asynchronous task processing [{}]. Instance will not be changed.",
						newInstanceId);
				//
				return new OperationResult
						.Builder(OperationState.NOT_EXECUTED)
						.setModel(
								new DefaultResultModel(
										CoreResultCode.CONFIGURATION_SWITCH_INSTANCE_NOT_CHANGED,
										ImmutableMap.of(
												ConfigurationService.PROPERTY_INSTANCE_ID, newInstanceId
										)
								)
						)
						.build();
			}
			// scheduled task at first
			int scheduledTaskCount = schedulerManager.switchInstanceId(previousInstanceId, newInstanceId);
			// then created lrt
			int longRunningTaskcount = taskManager.switchInstanceId(previousInstanceId, newInstanceId);
			// created events at last
			int eventCount = eventManager.switchInstanceId(previousInstanceId, newInstanceId);
			//
			return new OperationResult
					.Builder(OperationState.EXECUTED)
					.setModel(
							new DefaultResultModel(
									CoreResultCode.CONFIGURATION_SWITCH_INSTANCE_SUCCESS, 
									ImmutableMap.of(
											"previousInstanceId", previousInstanceId,
											"newInstanceId", newInstanceId,
											"scheduledTaskCount", String.valueOf(scheduledTaskCount),
											"longRunningTaskcount", String.valueOf(longRunningTaskcount),
											"eventCount", String.valueOf(eventCount)
									)
							)
					)
					.build();
		} finally {
			// property was created internally for this action only => we need log, but not property itself
			if (propertyCreated) {
				configurationService.delete(dto);
			}
		}
	}
	
	@Override
	public ResultModels prevalidate() {
		ResultModels models = new ResultModels();
		Set<String> previousInstanceIds = getPreviousInstanceIds(configurationService.getInstanceId());
		if (previousInstanceIds.size() > 1) {
			models.addInfo(
					new DefaultResultModel(
							CoreResultCode.CONFIGURATION_SWITCH_INSTANCE_MORE_PREVIOUS_FOUND,
							ImmutableMap.of("previousInstanceIds", StringUtils.join(previousInstanceIds, ", "))
					)
			);
		}
		//
		return models;
	}
	
	/**
	 * Possible previous instances -> different than currently and configured 
	 * for asynchronous event processing or configured for scheduled tasks.
	 * @param currentInstanceId
	 * @return
	 */
	private Set<String> getPreviousInstanceIds(String currentInstanceId) {
		Set<String> previousInstanceIds = new LinkedHashSet<>(1); // one in most cases
		//
		String currentEventInstanceId = eventConfiguration.getAsynchronousInstanceId();
		// ~ property in DB for events is probably related to previous setting
		if (!currentInstanceId.equals(currentEventInstanceId)) {
			previousInstanceIds.add(currentEventInstanceId);
		}
		// try to find scheduled task on other instance
		schedulerManager
			.getAllTasks()
			.forEach(task -> {
				String taskInstanceId = task.getInstanceId();
				//
				if (!taskInstanceId.equals(currentInstanceId)) {
					previousInstanceIds.add(taskInstanceId);
				}
			});
		//
		return previousInstanceIds;
	}
}
