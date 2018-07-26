package eu.bcvsolutions.idm.core.eav.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormValueService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import io.swagger.annotations.Api;

/**
 * Form definition attributes values
 *
 * @author Roman Kučera
 */

@PreAuthorize("hasAnyAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/form-definition-values")
@Api(
		value = IdmFormValueController.TAG,
		description = "Operations with form attribute values",
		tags = {IdmFormValueController.TAG},
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmFormValueController extends AbstractReadDtoController<IdmFormValueDto, IdmFormValueFilter> {

	protected static final String TAG = "Form attributes values";

	@Autowired
	public IdmFormValueController(IdmFormValueService service) {
		super(service);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

}
