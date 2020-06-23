package eu.bcvsolutions.idm.core.api.bulk.action;

import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;

/**
 * Interface for bulk operation
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmBulkAction<DTO extends BaseDto, F extends BaseFilter>
		extends Plugin<Class<? extends BaseEntity>>, Ordered {

	int DEFAULT_ORDER = 0;
	String PARAMETER_BULK_ACTION = "core:bulkAction"; // Persisted bulk action dto.
	
	/**
	 * Get list of form attributes
	 *
	 * @return
	 */
	List<IdmFormAttributeDto> getFormAttributes();

	/**
	 * Get name of bulk action
	 *
	 * @return
	 */
	String getName();
	
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
	 * Get module
	 *
	 * @return
	 */
	String getModule();
	
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
	public boolean isGeneric();
	
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
}
