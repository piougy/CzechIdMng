package eu.bcvsolutions.idm.core.eav.rest.impl;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import eu.bcvsolutions.idm.core.api.dto.FormableDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.rest.FormableDtoController;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.FormableDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.annotations.ApiParam;

/**
 * CRUD operations for formable DTO, which supports event processing.
 * 
 * TODO: move here other methods - require secured eav attributes to all controllers.
 * 
 * @see DataFilter
 * @param <DTO> dto type
 * @param <F> filter type
 * 
 * @author Radek Tomiška
 * @since 10.3.3
 */
public abstract class AbstractFormableDtoController<DTO extends FormableDto, F extends BaseFilter>
		extends AbstractEventableDtoController<DTO, F>
		implements FormableDtoController<DTO, F> {
	
	@Autowired private IdmFormDefinitionController formDefinitionController;
	//
	private final FormableDtoService<DTO, F> service;
	
	public AbstractFormableDtoController(FormableDtoService<DTO, F> service) {
		super(service);
		//
		this.service = service;
	}
	
	@Override
	public FormableDtoService<DTO, F> getService() {
		return service;
	}
	
	@Override
	public ResponseEntity<?> getFormDefinitions(
			@ApiParam(value = "Backend entity identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return formDefinitionController.getDefinitions(
				getService().getDtoClass(), 
				IdmBasePermission.AUTOCOMPLETE
		);
	}
	
	@Override
	public Resource<?> prepareFormValues(
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, required = false) String definitionCode) {
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(
				getService().getDtoClass(), 
				definitionCode, 
				IdmBasePermission.AUTOCOMPLETE);
		//
		// empty form instance with filled form definition
		IdmFormInstanceDto formInstance = new IdmFormInstanceDto();
		formInstance.setFormDefinition(formDefinition);
		formInstance.setOwnerType(getService().getEntityClass());
		// secure attributes
		formDefinitionController.secureAttributes(formInstance);
		//
		return new Resource<>(formInstance);
	}	
}
