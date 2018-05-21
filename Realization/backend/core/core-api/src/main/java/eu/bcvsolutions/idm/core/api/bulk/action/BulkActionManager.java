package eu.bcvsolutions.idm.core.api.bulk.action;

import java.util.List;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

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
	 * @param entity
	 * @return
	 */
	List<IdmBulkActionDto> getAvailableActions(Class<? extends AbstractEntity> entity);
	
	/**
	 * Process bulk action in new long running task
	 *
	 * @param actionDto
	 * @return
	 */
	IdmBulkActionDto processAction(IdmBulkActionDto actionDto);
	
}
