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

	List<IdmBulkActionDto> getAvailableActions(Class<? extends AbstractEntity> entity);
	
	IdmBulkActionDto processAction(IdmBulkActionDto actionDto);
	
}
