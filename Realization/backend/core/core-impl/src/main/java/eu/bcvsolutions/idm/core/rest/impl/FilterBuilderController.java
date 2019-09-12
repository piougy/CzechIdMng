package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.FilterBuilderDto;
import eu.bcvsolutions.idm.core.api.dto.filter.FilterBuilderFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * filter builders's administration.
 *
 * @author Kolychev Artem
 * @since 9.7.7
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/filter-builders")
@Api(
		value = FilterBuilderController.TAG,
		description = "Configure filter builders",
		tags = { FilterBuilderController.TAG },
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class FilterBuilderController  {

	protected static final String TAG = "filter builders filters";

    @Autowired private FilterManager filterManager;
    @Autowired private PagedResourcesAssembler<Object> pagedResourcesAssembler;

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_READ + "')")
	@ApiOperation(
			value = "Find all filter builders",
			nickname = "findAllFilterBuilders",
			tags = { FilterBuilderController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") })
				},
			notes = "Returns all registered filter builders.")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		List<FilterBuilderDto> filterBuilderDtos = filterManager.find(toFilter(parameters));
		PageImpl page = new PageImpl(filterBuilderDtos, new PageRequest(0, filterBuilderDtos.size() == 0 ? 10 : filterBuilderDtos.size()), filterBuilderDtos.size());
		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyResource(page, FilterBuilderDto.class, null);
		}
		return pagedResourcesAssembler.toResource(page);
	}
	
	/**
	 * Enable filter builder
	 * 
	 * @param filterId
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/{filterId}/enable", method = { RequestMethod.PATCH, RequestMethod.PUT })
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_UPDATE + "')")
	@ApiOperation(
			value = "Activate filter builder",
			nickname = "activateFilterBuilder",
			tags = { FilterBuilderController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.MODULE_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.MODULE_UPDATE, description = "") })
			})
	public void enable(
			@ApiParam(value = "Filter builder's identifier.", required = true)
			@PathVariable @NotNull String filterId) {
		filterManager.enable(filterId);
	}

    protected FilterBuilderFilter toFilter(MultiValueMap<String, Object> parameters) {
        return new FilterBuilderFilter(parameters);
    }
}
