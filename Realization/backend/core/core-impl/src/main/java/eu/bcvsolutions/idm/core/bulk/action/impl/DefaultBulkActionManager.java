package eu.bcvsolutions.idm.core.bulk.action.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

/**
 * Implementation of manager for bulk action.
 * 
 * TODO: find with filter / agenda on FE.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service("bulkActionManager")
public class DefaultBulkActionManager implements BulkActionManager {

	private final PluginRegistry<AbstractBulkAction<? extends BaseDto, ? extends BaseFilter>, Class<? extends BaseEntity>> pluginExecutors;
	private final LongRunningTaskManager taskManager;
	private final EnabledEvaluator enabledEvaluator;
	
	@Autowired
	public DefaultBulkActionManager(
			List<AbstractBulkAction<? extends BaseDto, ? extends BaseFilter>> actions,
			LongRunningTaskManager taskManager,
			EnabledEvaluator enabledEvaluator) {
		pluginExecutors = OrderAwarePluginRegistry.create(actions);
		//
		this.taskManager = taskManager;
		this.enabledEvaluator = enabledEvaluator;
	}
	
	@Override
	public IdmBulkActionDto processAction(IdmBulkActionDto action) {
		AbstractBulkAction<? extends BaseDto, ? extends BaseFilter> executor = getOperationForDto(action);
		//
		executor = createNewActionInstance(executor, action);
		//
		executor.setAction(action);
		//
		// validate before execute
		executor.validate();
		//
		LongRunningFutureTask<OperationResult> execute = taskManager.execute(executor);
		action.setLongRunningTaskId(execute.getExecutor().getLongRunningTaskId());
		action.setEntityClass(executor.getService().getEntityClass().getName());
		action.setFilterClass(executor.getService().getFilterClass().getName());
		action.setModule(executor.getModule());
		action.setFormAttributes(executor.getFormAttributes());
		//
		action.setAuthorities(executor.getAuthorities());
		return action;
	}
	
	@Override
	public ResultModels prevalidate(IdmBulkActionDto action) {
		AbstractBulkAction<? extends BaseDto, ? extends BaseFilter> executor = getOperationForDto(action);
		//
		executor = createNewActionInstance(executor, action);
		//
		executor.setAction(action);
		//
		// Prevalidate before execute
		return executor.prevalidate();
	}
	
	@Override
	public List<IdmBulkActionDto> getAvailableActions(Class<? extends BaseEntity> entity) {

		List<AbstractBulkAction<? extends BaseDto, ? extends BaseFilter>> enabledActions = getEnabledActions(entity);
		return enabledActions.stream()
				.map(action -> {
					/*
					 * If is action generic, then we need to create new instance
					 * and set entity class to it in every case (includes
					 * getAvailableActions too). Stateful actions are typically
					 * generic actions (uses for more than one entity type). That
					 * action doesn't have knowledge about entity type in stateless
					 * mode.
					 */
					if (action.isGeneric()) {
						return createNewActionInstance(action, entity);
					}
					return action;
				})
				.map(this::toDto)
				.collect(Collectors.toList());
	}

	/**
	 * Create new instance of a bulk action bean.
	 * 
	 * @param action
	 * @param actionDto
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	private AbstractBulkAction<?, ?> createNewActionInstance(AbstractBulkAction<? extends BaseDto, ? extends BaseFilter> action, IdmBulkActionDto actionDto) {
		if (!action.isGeneric()) {
			// Entity class will be set only for generic actions (performance reason, prevents useless loading a class from string).
			return createNewActionInstance(action, (Class<? extends BaseEntity>) null);
		}
		try {
			Class<?> forName = Class.forName(actionDto.getEntityClass());
			if (AbstractEntity.class.isAssignableFrom(forName)) {
				return createNewActionInstance(action, (Class<? extends BaseEntity>) forName);
			}
		} catch (ClassNotFoundException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("bulkActionClass", actionDto.getEntityClass()), e);
		}
		throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("bulkActionName", actionDto.getName()));
	}
	
	/**
	 * Create new instance of a bulk action bean.
	 * 
	 * @param action
	 * @param entity
	 * @return 
	 */
	private AbstractBulkAction<?, ?> createNewActionInstance(AbstractBulkAction<? extends BaseDto, ? extends BaseFilter> action, Class<? extends BaseEntity> entity) {
		AbstractBulkAction<?, ?> statefulAction = (AbstractBulkAction<?, ?>) AutowireHelper.createBean(AutowireHelper.getTargetClass(action));
		if (statefulAction.getEntityClass() == null) {
			statefulAction.setEntityClass(entity);
		}
		return statefulAction;
	}

	@SuppressWarnings("unchecked")
	private AbstractBulkAction<? extends BaseDto, ? extends BaseFilter> getOperationForDto(IdmBulkActionDto actionDto) {
		Assert.notNull(actionDto, "Action DTO is required to get action executor.");
		Assert.notNull(actionDto.getEntityClass(), "Action has to be assigned to some entity, which can action process.");
		try {
			Class<?> forName = Class.forName(actionDto.getEntityClass());
			if (AbstractEntity.class.isAssignableFrom(forName)) {
				List<AbstractBulkAction<? extends BaseDto, ? extends BaseFilter>> actions = getEnabledActions((Class<? extends BaseEntity>) forName);
				//
				for (AbstractBulkAction<? extends BaseDto, ? extends BaseFilter> action : actions) {
					// skip disabled modules 
					if (!enabledEvaluator.isEnabled(action)) {
						continue;
					}
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
	
	/**
	 * Map action to dto.
	 * 
	 * @param action
	 * @return
	 */
	private IdmBulkActionDto toDto(AbstractBulkAction<? extends BaseDto, ? extends BaseFilter> action) {
		IdmBulkActionDto actionDto = new IdmBulkActionDto();
		actionDto.setId(action.getName()); // FIXME: spring bean name 
		actionDto.setEntityClass(action.getService() != null ? action.getService().getEntityClass().getName() : null);
		actionDto.setFilterClass(action.getService() != null ? action.getService().getFilterClass().getName() : null);
		actionDto.setModule(action.getModule());
		actionDto.setName(action.getName());
		actionDto.setDescription(action.getDescription());
		actionDto.setFormAttributes(action.getFormAttributes());
		actionDto.setAuthorities(action.getAuthorities());
		actionDto.setShowWithoutSelection(action.showWithoutSelection());
		actionDto.setShowWithSelection(action.showWithSelection());
		actionDto.setDisabled(action.isDisabled());
		actionDto.setLevel(action.getLevel());
		//
		return actionDto;
	}
	
	/**
	 * Get enabled actions
	 * 
	 * @param entity
	 * @return
	 */
	private List<AbstractBulkAction<? extends BaseDto, ? extends BaseFilter>> getEnabledActions(Class<? extends BaseEntity> entity) {
		return pluginExecutors
				.getPluginsFor(entity)
				.stream()
				.filter(action -> enabledEvaluator.isEnabled(action))
				.collect(Collectors.toList());
	}
}
