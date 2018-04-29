package eu.bcvsolutions.idm.core.api.bulk.operation;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.bulk.operation.dto.IdmBulkOperationDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * Interface for bulk operation
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmBulkOperation<DTO extends BaseDto>
		extends Plugin<Class<? extends AbstractEntity>> {

	List<IdmFormAttributeDto> getFormAttributes();

	String getName();
	
	IdmBulkOperationDto getOperation();

	void setOperation(IdmBulkOperationDto operation);
	
	String getFilterClass();
	
	String getEntityClass();
	
	String getModule();
	
}
