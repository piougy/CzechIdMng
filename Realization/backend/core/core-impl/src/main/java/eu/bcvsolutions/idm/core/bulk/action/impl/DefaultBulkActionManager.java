package eu.bcvsolutions.idm.core.bulk.action.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.OrderComparator;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.IdmBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.BulkActionFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

/**
 * Implementation of manager for bulk action.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@Service("bulkActionManager")
public class DefaultBulkActionManager implements BulkActionManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultBulkActionManager.class);
	//
	private final PluginRegistry<AbstractBulkAction<? extends BaseDto, ? extends BaseFilter>, Class<? extends BaseEntity>> pluginExecutors;
	private final LongRunningTaskManager taskManager;
	private final EnabledEvaluator enabledEvaluator;
	private final ParameterConverter parameterConverter;
	//
	@Autowired private ApplicationContext context;
	@Autowired @Lazy private ConfigurationService configurationService;
	
	@Autowired
	public DefaultBulkActionManager(
			List<AbstractBulkAction<? extends BaseDto, ? extends BaseFilter>> actions,
			LongRunningTaskManager taskManager,
			EnabledEvaluator enabledEvaluator) {
		pluginExecutors = OrderAwarePluginRegistry.create(actions);
		//
		this.taskManager = taskManager;
		this.enabledEvaluator = enabledEvaluator;
		this.parameterConverter = new ParameterConverter(); // lookup service is not needed now
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
		if (executor.getService().getEntityClass() != null) {
			action.setEntityClass(executor.getService().getEntityClass().getName());
		}
		if (executor.getService().getFilterClass() != null) {
			action.setFilterClass(executor.getService().getFilterClass().getName());
		}
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
				.sorted(OrderComparator.INSTANCE)
				.collect(Collectors.toList());
	}
	
	@Override
	public List<IdmBulkActionDto> getAvailableActionsForDto(Class<? extends BaseDto> dtoClass) {

		List<AbstractBulkAction<? extends BaseDto, ? extends BaseFilter>> enabledActions = getEnabledActionsForDto(dtoClass);
		return enabledActions.stream()
				.map(action -> {
					return action;
				})
				.map(this::toDto)
				.sorted(OrderComparator.INSTANCE)
				.collect(Collectors.toList());
	}
	
	@Override
	public List<IdmBulkActionDto> find(BulkActionFilter filter) {
		List<IdmBulkActionDto> dtos = new ArrayList<>();
		pluginExecutors
			.getPlugins()
			.stream()
			.forEach(action -> {
				// bulk actions depends on module - we could not call any processor method
				if (enabledEvaluator.isEnabled(action)) {
					IdmBulkActionDto dto = toDto(action);
					//
					if (passFilter(dto, filter)) {
						dtos.add(dto);
					}
				}
			});
		//
		// sort by order
		Collections.sort(dtos, new Comparator<IdmBulkActionDto>() {

			@Override
			public int compare(IdmBulkActionDto one, IdmBulkActionDto two) {
				return Integer.compare(one.getOrder(),two.getOrder());
			}
			
		});
		//
		LOG.debug("Returning [{}] registered bulk actions", dtos.size());
		//
		return dtos;
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

		if (actionDto.getEntityClass() != null) {
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

		if (actionDto.getDtoClass() != null) {
			try {
				Class<?> forName = Class.forName(actionDto.getDtoClass());
				if (BaseDto.class.isAssignableFrom(forName)) {
					List<AbstractBulkAction<? extends BaseDto, ? extends BaseFilter>> actions = getEnabledActionsForDto((Class<? extends BaseDto>) forName);
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
				throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("bulkActionClass", actionDto.getDtoClass()), e);
			}
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("bulkActionName", actionDto.getName()));
		}
		return null;
	}
	
	/**
	 * Map action to dto.
	 * 
	 * @param action
	 * @return
	 */
	@Override
	public IdmBulkActionDto toDto(AbstractBulkAction<? extends BaseDto, ? extends BaseFilter> action) {
		IdmBulkActionDto actionDto = new IdmBulkActionDto();
		actionDto.setId(action.getId());
		if (action.getService() != null && action.getService().getEntityClass() != null) {
			actionDto.setEntityClass(action.getService().getEntityClass().getCanonicalName());
		} else {
			Class<? extends BaseEntity> entityClass = action.getEntityClass();
			if (entityClass != null) {
				actionDto.setEntityClass(entityClass.getCanonicalName());
			}			
		}
		if (action.getDtoClass() != null) {
			actionDto.setDtoClass(action.getDtoClass().getName());
		}
		if (action.getService() != null && action.getService().getFilterClass() != null) {
			actionDto.setFilterClass(action.getService().getFilterClass().getName());
		}
		actionDto.setModule(action.getModule());
		actionDto.setName(action.getName());
		actionDto.setDescription(action.getDescription());
		actionDto.setFormAttributes(action.getFormAttributes());
		actionDto.setAuthorities(action.getAuthorities());
		actionDto.setShowWithoutSelection(action.showWithoutSelection());
		actionDto.setShowWithSelection(action.showWithSelection());
		actionDto.setDisabled(action.isDisabled());
		ConfigurationMap configurationMap = action.getConfigurationMap();
		//
		// set configurable properties
		actionDto.setIcon(parameterConverter.toString(configurationMap, ConfigurationService.PROPERTY_ICON));
		actionDto.setOrder((int) parameterConverter.toLong(configurationMap, ConfigurationService.PROPERTY_ORDER, action.getOrder())); // FIXME: add ParameterConverter#toInteger method instead.
		actionDto.setDeleteAction(parameterConverter.toBoolean(configurationMap, IdmBulkAction.PROPERTY_DELETE_ACTION, action.isDeleteAction()));
		actionDto.setQuickButton(parameterConverter.toBoolean(configurationMap, IdmBulkAction.PROPERTY_QUICK_BUTTON, action.isQuickButton()));
		actionDto.setQuickButtonable(parameterConverter.toBoolean(configurationMap, IdmBulkAction.PROPERTY_QUICK_BUTTONABLE, action.isQuickButtonable()));
		actionDto.setLevel(action.getLevel());
		try {
			NotificationLevel level = parameterConverter.toEnum(configurationMap, ConfigurationService.PROPERTY_LEVEL, NotificationLevel.class);
			if (level != null) {
				actionDto.setLevel(level);
			}
		} catch (ResultCodeException ex) {
			LOG.warn("Configuration property [{}] is wrongly configured, given [{}]. Default action level [{}] will be used.", 
					action.getConfigurationPropertyName(ConfigurationService.PROPERTY_LEVEL),
					configurationMap.get(ConfigurationService.PROPERTY_LEVEL),
					action.getLevel());
		}
		//
		return actionDto;
	}
	
	@Override
	public void enable(String bulkActionId) {
		setEnabled(bulkActionId, true);
	}

	@Override
	public void disable(String bulkActionId) {
		setEnabled(bulkActionId, false);
	}

	@Override
	public void setEnabled(String bulkActionId, boolean enabled) {
		setEnabled(getAction(bulkActionId), enabled);
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
				.filter(enabledEvaluator::isEnabled)
				.filter(action -> !action.isDisabled())
				.sorted(OrderComparator.INSTANCE)
				.collect(Collectors.toList());
	}
	
	/**
	 * Get enabled actions
	 * 
	 * @param entity
	 * @return
	 */
	private List<AbstractBulkAction<? extends BaseDto, ? extends BaseFilter>> getEnabledActionsForDto(Class<? extends BaseDto> dtoClass) {
		return pluginExecutors
				.getPlugins()
				.stream()
				.filter(enabledEvaluator::isEnabled)
				.filter(action -> !action.isDisabled())
				.filter(action -> !action.isGeneric()) // Generic actions are not supported for DTO now.
				.filter(action -> dtoClass.equals(action.getDtoClass()))
				.collect(Collectors.toList());
	}
	
	/**
	 * Returns true, when given processor pass given filter.
	 * 
	 * @param action
	 * @param filter
	 * @return
	 */
	private boolean passFilter(IdmBulkActionDto action, BulkActionFilter filter) {
		if (filter == null) {
			// empty filter
			return true;
		}
		// id - not supported
		if (filter.getId() != null) {
			throw new UnsupportedOperationException("Filtering bulk actions by [id] is not supported.");
		}
		// text - lowercase like in name, description, entity class - canonical name
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			text = text.toLowerCase();
			if (!action.getName().toLowerCase().contains(text)
					&& (action.getDescription() == null || !action.getDescription().toLowerCase().contains(text))
					&& (action.getEntityClass() == null || !action.getEntityClass().toLowerCase().contains(text))) {
				return false;
			}
		}
		// action name
		String name = filter.getName();
		if (StringUtils.isNotEmpty(name) && !action.getName().equals(name)) {
			return false; 
		}
		// module id
		String module = filter.getModule();
		if (StringUtils.isNotEmpty(module) && !module.equals(action.getModule())) {
			return false;
		}
		// description - like
		String description = filter.getDescription();
		if (StringUtils.isNotEmpty(description) 
				&& !StringUtils.contains(description, action.getDescription())) {
			return false;
		}
		// entity class name
		String entityClass = filter.getEntityClass();
		String actionEntityClass = action.getEntityClass();
		if (StringUtils.isNotEmpty(entityClass)
				&& StringUtils.isNotEmpty(actionEntityClass) // generic bulk actions
				&& !entityClass.equals(actionEntityClass)) {
			return false;
		}
		//
		return true;
	}
	
	private IdmBulkAction<?, ?> getAction(String bulkActionId) {
		Assert.notNull(bulkActionId, "Bulk action identifier is required.");
		//
		return (IdmBulkAction<?, ?>) context.getBean(bulkActionId);
	}
	
	private void setEnabled(IdmBulkAction<?, ?> action, boolean enabled) {
		String enabledPropertyName = action.getConfigurationPropertyName(ConfigurationService.PROPERTY_ENABLED);
		//
		configurationService.setBooleanValue(enabledPropertyName, enabled);
	}
}
