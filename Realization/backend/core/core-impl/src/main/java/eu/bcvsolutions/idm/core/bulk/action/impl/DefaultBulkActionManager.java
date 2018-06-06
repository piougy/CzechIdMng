package eu.bcvsolutions.idm.core.bulk.action.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.IdmBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;

/**
 * Implementation of manager for bulk action
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service("bulkActionManager")
public class DefaultBulkActionManager implements BulkActionManager {

	private final PluginRegistry<AbstractBulkAction<? extends BaseDto>, Class<? extends BaseEntity>> pluginExecutors;
	private final LongRunningTaskManager taskManager;
	private final ModuleService moduleService;
	
	@Autowired
	public DefaultBulkActionManager(
			List<AbstractBulkAction<? extends BaseDto>> actions,
			LongRunningTaskManager taskManager,
			ModuleService moduleService) {
		pluginExecutors = OrderAwarePluginRegistry.create(actions);
		//
		this.taskManager = taskManager;
		this.moduleService = moduleService;
	}
	
	@Override
	public IdmBulkActionDto processAction(IdmBulkActionDto action) {
		AbstractBulkAction<? extends BaseDto> executor = getOperationForDto(action);
		// check if action is available
		if (!moduleService.isEnabled(executor.getModule())) {
			throw new ResultCodeException(CoreResultCode.BULK_ACTION_MODULE_DISABLED, ImmutableMap.of("action", action.getName(), "module", executor.getModule()));
		}
		//
		executor = (AbstractBulkAction<?>) AutowireHelper.createBean(executor.getClass());
		//
		executor.setAction(action);
		//
		// validate before execute
		executor.validate();
		//
		LongRunningFutureTask<OperationResult> execute = taskManager.execute(executor);
		action.setLongRunningTaskId(execute.getExecutor().getLongRunningTaskId());
		action.setEntityClass(executor.getEntityClass());
		action.setFilterClass(executor.getFilterClass());
		action.setModule(executor.getModule());
		action.setFormAttributes(executor.getFormAttributes());
		action.setPermissions(executor.getPermissions());
		return action;
	}
	
	@Override
	public List<IdmBulkActionDto> getAvailableActions(
			Class<? extends BaseEntity> entity) {
		List<AbstractBulkAction<? extends BaseDto>> actions = pluginExecutors.getPluginsFor(entity);
		//
		List<IdmBulkActionDto> result = new ArrayList<>();
		for (IdmBulkAction<? extends BaseDto> action : actions) {
			// skip disabled modules 
			if (!moduleService.isEnabled(action.getModule())) {
				continue;
			}
			IdmBulkActionDto actionDto = new IdmBulkActionDto();
			actionDto.setEntityClass(action.getEntityClass());
			actionDto.setFilterClass(action.getFilterClass());
			actionDto.setModule(action.getModule());
			actionDto.setName(action.getName());
			actionDto.setFormAttributes(action.getFormAttributes());
			actionDto.setPermissions(action.getPermissions());
			result.add(actionDto);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private AbstractBulkAction<? extends BaseDto> getOperationForDto(IdmBulkActionDto actionDto) {
		Assert.notNull(actionDto);
		Assert.notNull(actionDto.getEntityClass());
		try {
			Class<?> forName = Class.forName(actionDto.getEntityClass());
			if (AbstractEntity.class.isAssignableFrom(forName)) {
				List<AbstractBulkAction<?>> actions = pluginExecutors.getPluginsFor((Class<? extends BaseEntity>) forName);
				//
				for (AbstractBulkAction<? extends BaseDto> action : actions) {
					if (action.getName().equals(actionDto.getName())) {
						return action;
					}
				}
			}
		} catch (ClassNotFoundException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("bulkActionClass", actionDto.getEntityClass()), e);
		}
		throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("bulkActionName", actionDto.getName()));
	}

}
