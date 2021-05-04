package eu.bcvsolutions.idm.core.api.bulk.action;

import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;

/**
 * Interface for bulk operation.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public interface IdmBulkAction<DTO extends BaseDto, F extends BaseFilter> extends 
		LongRunningTaskExecutor<OperationResult>,
		Plugin<Class<? extends BaseEntity>>, 
		Ordered, 
		Configurable {

	/**
	 * Default bulk action order
	 */
	int DEFAULT_ORDER = 0;
	/**
	 * Persisted bulk action dto in LRT parameters (run bulk action async from LRT queue).
	 */
	String PARAMETER_BULK_ACTION = "core:bulkAction";
	/**
	 * Configurable property for include action in delete action menu (bottom).
	 * 
	 * @since 10.6.0
	 */
	String PROPERTY_DELETE_ACTION = "deleteAction";
	/**
	 * Configurable property for include action in quick access buttons.
	 * 
	 * @since 10.6.0
	 */
	String PROPERTY_QUICK_BUTTON = "quickButton";
	/**
	 * Configurable property for posibility to include action in quick access buttons.
	 * 
	 * @since 11.1.0
	 */
	String PROPERTY_QUICK_BUTTONABLE = "quickButtonable";
	/**
	 * Bulk action configurable type.
	 * 
	 * @since 10.6.0
	 */
	String CONFIGURABLE_TYPE = "bulk-action";

	@Override
	default String getConfigurableType() {
		return CONFIGURABLE_TYPE;
	}
	
	/**
	 * Get bulk action
	 *
	 * @return
	 */
	IdmBulkActionDto getAction();

	/**
	 * Set bulk action
	 *
	 * @param action
	 */
	void setAction(IdmBulkActionDto action);
	
	/**
	 * Return service. With the service will be executed bulk action.
	 *
	 * @return
	 */
	ReadWriteDtoService<DTO, F> getService();
	
	/**
	 * Validate given bulk action. Is necessary specify the action by setter {@link IdmBulkAction#setAction(IdmBulkActionDto)}
	 */
	void validate();
	
	/**
	 * Return authorities required for process one item.
	 *
	 * @return
	 */
	List<String> getAuthorities();

	/**
	 * Prevalidate method is called before start the action. It used for show additional informations to the user.
	 * 
	 * @return
	 */
	ResultModels prevalidate();
	
	/**
	 * Generic action.
	 *
	 * If is action generic, then we need to create new instance and set entity
	 * class to it in every case (includes getAvailableActions too). Stateful actions
	 * are typically generic actions (uses for more than one entity type). That
	 * action doesn't have knowledge about entity type by default. And this is a way
	 * how we can propagete entity type to it.
	 *
	 * @return
	 */
	boolean isGeneric();
	
	/**
	 * Returns {@code true}, when action can be executed without items are selected.
	 * Returns {@code false} by default.
	 * 
	 * @return
	 * @since 9.2.2
	 */
	default boolean showWithoutSelection() {
		return false;
	}
	
	/**
	 * Returns {@code true}, when action can be executed with items are selected (items identifiers or filter is given).
	 * Returns {@code true} by default.
	 * 
	 * @return
	 * @since 9.2.2
	 */
	default boolean showWithSelection() {
		return true;
	}
	
	/**
	 * Returns {@link NotificationLevel}, for decorate action importance / danger (e.g. actions for delete should have {@link NotificationLevel#ERROR}).
	 * Returns {@link NotificationLevel#SUCCESS} by default (~backward compatible).
	 * 
	 * @return
	 */
	default NotificationLevel getLevel() {
		return NotificationLevel.SUCCESS;
	}
	
	/**
	 * Action deletes records. This action will be sorted on the end section on FE.
	 * 
	 * @return false by default. true - action deletes records
	 * @since 10.6.0
	 */
	default boolean isDeleteAction() {
		return false;
	}
	
	/**
	 * Action will be included in quick buttons on FE. 
	 * Action can be shown as quick button, when icon is defined (in locale or by icon property).
	 * Returns {@code false} by default.
	 * 
	 * @return true - action button will be shown, if icon is defined.
	 * @since 10.6.0
	 */
	default boolean isQuickButton() {
		return false;
	}
	
	/**
	 * Action can be included in quick buttons on FE. 
	 * Action can be shown as quick button, when icon is defined (in locale or by icon property).
	 * Returns {@code true} by default.
	 * 
	 * @return false - action button will not be shown
	 * @since 11.0.0
	 */
	default boolean isQuickButtonable() {
		return true;
	}
}
