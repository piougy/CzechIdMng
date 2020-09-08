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

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.filter.BulkActionFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.FilterConverter;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Bulk action administration.
 * 
 * Be careful: page and size is not implemented in find methods.
 * 
 * @author Radek Tomiška
 * @since 10.6.0
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/bulk-actions")
@Api(
		value = BulkActionController.TAG, 
		description = "Configure bulk actions", 
		tags = { BulkActionController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class BulkActionController {

	protected static final String TAG = "Bulk action administration";	
	//
	@Autowired private BulkActionManager bulkActionManager;
	@Autowired private PagedResourcesAssembler<Object> pagedResourcesAssembler;
	@Autowired private ObjectMapper mapper;
	@Autowired private LookupService lookupService;
	//
	private FilterConverter filterConverter;
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_READ + "')")
	@ApiOperation(
			value = "Find all bulk actions", 
			nickname = "findAllBulkActions", 
			tags = { BulkActionController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") })
				},
			notes = "Returns all registered bulk actions with state properties (disabled, order).")
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		List<IdmBulkActionDto> records = bulkActionManager.find(toFilter(parameters));
		PageImpl page = new PageImpl(records, PageRequest.of(0, records.size() == 0 ? 10 : records.size()), records.size());
		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyResource(page, IdmBulkActionDto.class);
		}
		return pagedResourcesAssembler.toResource(page);
	}
	
	/**
	 * Enable bulk action.
	 * 
	 * @param bulkActionId
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/{bulkActionId}/enable", method = { RequestMethod.PATCH, RequestMethod.PUT })
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_UPDATE + "')")
	@ApiOperation(
			value = "Enable bulk action",
			nickname = "enableBulkAction",
			tags = { BulkActionController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.MODULE_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.MODULE_UPDATE, description = "") })
			})
	public void enable(
			@ApiParam(value = "Bulk action identifier.", required = true)
			@PathVariable @NotNull String bulkActionId) {
		bulkActionManager.enable(bulkActionId);
	}
	
	/**
	 * Disable bulk action.
	 * 
	 * @param bulkActionId
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/{bulkActionId}/disable", method = { RequestMethod.PATCH, RequestMethod.PUT })
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_UPDATE + "')")
	@ApiOperation(
			value = "Disable bulk action",
			nickname = "disableBulkAction",
			tags = { BulkActionController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.MODULE_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.MODULE_UPDATE, description = "") })
			})
	public void disable(
			@ApiParam(value = "Bulk action identifier.", required = true)
			@PathVariable @NotNull String bulkActionId) {
		bulkActionManager.disable(bulkActionId);
	}

	/**
	 * Return parameter converter helper.
	 * 
	 * @return
	 */
	protected FilterConverter getParameterConverter() {
		if (filterConverter == null) {
			filterConverter = new FilterConverter(lookupService, mapper);
		}
		return filterConverter;
	}
	
	private BulkActionFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new BulkActionFilter(parameters, getParameterConverter());
	}
}
