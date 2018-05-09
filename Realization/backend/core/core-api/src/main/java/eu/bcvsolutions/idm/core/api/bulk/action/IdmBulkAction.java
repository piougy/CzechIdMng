package eu.bcvsolutions.idm.core.api.bulk.action;

import java.util.List;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * Interface for bulk operation
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmBulkAction<DTO extends BaseDto>
		extends Plugin<Class<? extends AbstractEntity>> {

	List<IdmFormAttributeDto> getFormAttributes();

	String getName();
	
	IdmBulkActionDto getAction();

	void setAction(IdmBulkActionDto action);
	
	String getFilterClass();
	
	String getEntityClass();
	
	String getModule();
	
	void validate();
}
