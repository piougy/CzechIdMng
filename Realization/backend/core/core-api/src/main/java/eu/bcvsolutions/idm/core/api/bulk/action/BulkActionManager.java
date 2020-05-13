package eu.bcvsolutions.idm.core.api.bulk.action;

import java.util.List;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Interface for bulk action manager
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
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
	
}
