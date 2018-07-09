package eu.bcvsolutions.idm.core.eav.rest.impl;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionAttributesValuesService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormValue;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

/**
 * Form definition attributes values
 *
 * @author Roman Kučera
 */

@PreAuthorize("hasAnyAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/form-definition-values")
@Api(
		value = IdmFormAttributeValuesController.TAG,
		description = "Operations with form attribute values",
		tags = {IdmFormAttributeValuesController.TAG},
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmFormAttributeValuesController extends AbstractReadWriteDtoController<IdmFormValueDto, IdmFormValueFilter> {

	protected static final String TAG = "Form attributes values";

	@Autowired
	private IdmFormDefinitionAttributesValuesService service;
	@Autowired
	private PagedResourcesAssembler<Object> pagedResourcesAssembler;

	@Autowired
	public IdmFormAttributeValuesController(IdmFormDefinitionAttributesValuesService service) {
		super(service);
		this.service = service;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
//		List<IdmFormValueDto> result = service.findDefinitionAttributesValues((String) parameters.toSingleValueMap().get("definitionId"));
//		PageImpl page = new PageImpl(result, new PageRequest(0, result.size() == 0 ? 10 : result.size()), result.size());
//		if (page.getContent().isEmpty()) {
//			return pagedResourcesAssembler.toEmptyResource(page, IdmFormValueDto.class, null);
//		}
//		return pagedResourcesAssembler.toResource(page);
		return super.find(parameters, pageable);
	}

}
