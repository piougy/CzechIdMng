package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.AvailableServiceDto;
import eu.bcvsolutions.idm.core.api.dto.filter.AvailableServiceFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * available service administration.
 *
 * @author Ondrej Husnik
 *
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/available-service")
@Api(
		value = AvailableServiceController.TAG,
		description = "Displays available services",
		tags = { AvailableServiceController.TAG },
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class AvailableServiceController  {

	protected static final String TAG = "available service";

    @Autowired private IdmScriptAuthorityService availableServiceService;
    @Autowired private PagedResourcesAssembler<Object> pagedResourcesAssembler;

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@ApiOperation(
			value = "Find all available services",
			nickname = "findAllAvailableServices",
			tags = { AvailableServiceController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") })
				},
			notes = "Returns all available services.")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
	List<AvailableServiceDto> serviceDtos = availableServiceService.findServices(toFilter(parameters));
		PageImpl page = new PageImpl(serviceDtos, PageRequest.of(0, serviceDtos.size() == 0 ? 10 : serviceDtos.size()), serviceDtos.size());
		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyResource(page, AvailableServiceDto.class);
		}
		return pagedResourcesAssembler.toResource(page);
	}
	
	
    protected AvailableServiceFilter toFilter(MultiValueMap<String, Object> parameters) {
        return new AvailableServiceFilter(parameters);
    }
}
