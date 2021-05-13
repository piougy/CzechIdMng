package eu.bcvsolutions.idm.core.api.bulk.action;

import java.util.List;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.BulkActionFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Interface for bulk action manager.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public interface BulkActionManager {

	/**
	 * Get all available bulk actions for given entity
	 *
	 * @param entity action for given entity
	 * @return available bulk actions.
	 */
	List<IdmBulkActionDto> getAvailableActions(Class<? extends BaseEntity> entity);
	
	/**
	 * Get all available bulk actions for given entity.
	 *
	 * @param dtoClass action for given dtoClass
	 * @return available bulk actions.
	 */
	List<IdmBulkActionDto> getAvailableActionsForDto(Class<? extends BaseDto> dtoClass);
	
	/**
	 * Returns all registered bulk actions by given filter.
	 * 
	 * @param filter filter
	 * @return registered bulk actions by filter
	 * @since 10.6.0
	 */
	List<IdmBulkActionDto> find(BulkActionFilter filter);
	
	/**
	 * Process bulk action in new long running task
	 *
	 * @param actionDto action configuration.
	 *
	 * @return processing bulk action with long running tastk identifier is filled.
	 *         Action is processed asynchronously - process can be get by long
	 *         running task identifier in LRT agenda.
	 */
	IdmBulkActionDto processAction(IdmBulkActionDto actionDto);

	/**
	 * Prevalidate bulk action.
	 * 
	 * @param bulkAction action configuration.
	 * @return Messages - what action will or will not do.
	 */
	ResultModels prevalidate(IdmBulkActionDto bulkAction);
	
	/**
	 * To DTO conversion.
	 * 
	 * @param action
	 * @return 
	 */
	IdmBulkActionDto toDto(AbstractBulkAction<? extends BaseDto, ? extends BaseFilter> action);
	
	/**
	 * Enable given bulk action. 
	 * Throws {@link IllegalArgumentException} when bulkActionId is not installed.
	 * 
	 * @param bulkActionId id ~ bean name
	 * @since 10.6.0
	 */
	void enable(String bulkActionId);

	/**
	 * Disable given bulk action. 
	 * Throws {@link IllegalArgumentException} when bulkActionId is not installed.
	 * 
	 * @param bulkActionId id ~ bean name
	 * @since 10.6.0
	 */
	void disable(String bulkActionId);

	/**
	 * Enable / disable given bulk action.
	 *
	 * @param bulkActionId id ~ bean name
	 * @param enabled true - enabled, false - disabled
	 * @since 10.6.0
	 */
	void setEnabled(String bulkActionId, boolean enabled);
}
