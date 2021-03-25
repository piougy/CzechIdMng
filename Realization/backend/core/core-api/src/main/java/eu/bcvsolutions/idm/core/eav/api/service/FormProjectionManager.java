package eu.bcvsolutions.idm.core.eav.api.service;

import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.eav.api.dto.FormProjectionRouteDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;

/**
 * Provides supported form projections.
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
public interface FormProjectionManager {
	
	/**
	 * Returns supported form projection routes.
	 * 
	 * @return registered form projection routes
	 */
	List<FormProjectionRouteDto> getSupportedRoutes();
	
	/**
	 * Return form definition for basic fields.
	 * 
	 * @param dto owner
	 * @return for definition
	 * @since 11.0.0
	 */
	IdmFormDefinitionDto getBasicFieldsDefinition(BaseDto dto);
	
	/**
	 * Return form instance (with form definition) for basic fields.
	 * 
	 * @param dto owner
	 * @return form instance with definition and values by dto fields
	 * @since 11.0.0
	 */
	IdmFormInstanceDto getBasicFieldsInstance(BaseDto dto);
}
