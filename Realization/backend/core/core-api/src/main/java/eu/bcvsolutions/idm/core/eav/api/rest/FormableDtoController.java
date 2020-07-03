package eu.bcvsolutions.idm.core.eav.api.rest;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import eu.bcvsolutions.idm.core.api.dto.FormableDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import io.swagger.annotations.ApiParam;

/**
 * CRUD operations for formable DTO, which supports event processing.
 * 
 * TODO: all formable values methods.
 * 
 * @see DataFilter
 * @param <DTO> dto type
 * @param <F> filter type
 * 
 * @author Radek Tomiška
 * @since 10.3.3
 */
public interface FormableDtoController<DTO extends FormableDto, F extends BaseFilter> {
	
	/**
	 * Returns form definition to given entity.
	 * 
	 * @param backendId
	 * @return
	 */
	ResponseEntity<?> getFormDefinitions(
			@ApiParam(value = "Backend entity identifier.", required = true)
			@PathVariable @NotNull String backendId);
	
	/**
	 * Prepare form values for newly created record.
	 * 
	 * @param definitionCode [optional] form definition od default
	 * @return
	 */
	Resource<?> prepareFormValues(
			@ApiParam(
					value = "Code of form definition (default will be used if no code is given).",
					required = false,
					defaultValue = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(
					name = IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE,
					required = false)
			String definitionCode);
}
