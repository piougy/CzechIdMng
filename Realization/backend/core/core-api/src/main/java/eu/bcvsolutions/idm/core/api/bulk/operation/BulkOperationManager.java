package eu.bcvsolutions.idm.core.api.bulk.operation;

import java.util.List;

import eu.bcvsolutions.idm.core.api.bulk.operation.dto.IdmBulkOperationDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Interface for bulk operation mabager
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface BulkOperationManager {

	List<IdmBulkOperationDto> getAvailableOperations(Class<? extends AbstractEntity> entity);
	
	IdmBulkOperationDto processOperation(IdmBulkOperationDto operationDto);
	
	
	void test();
}
