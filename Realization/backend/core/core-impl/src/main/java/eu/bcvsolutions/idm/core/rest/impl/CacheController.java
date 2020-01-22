package eu.bcvsolutions.idm.core.rest.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.api.dto.IdmCacheDto;
import io.swagger.annotations.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cache controller - provides clear cache functionality
 *
 * @author Peter Štrunc
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/caches")
@Api(
		value = CacheController.TAG,
		tags = { CacheController.TAG },
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class CacheController {

	protected static final String TAG = "Cache";
	//
	@Autowired private IdmCacheManager cacheManager;
	//
	private ParameterConverter parameterConverter = null;
	@Autowired private PagedResourcesAssembler<IdmCacheDto> pagedResourcesAssembler;

	/**
	 * Returns all available caches
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_READ + "')")
	@ApiOperation(
			value = "Get all available caches",
			nickname = "getAvailableCaches",
			tags = { CacheController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") })
				})
	public PagedResources<Resource<IdmCacheDto>> getAvailableCaches() {

		return pagedResourcesAssembler.toResource(cacheManager.getAllAvailableCaches());
	}

	/**
	 * Returns selected module
	 *
	 * @param moduleId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{moduleId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_READ + "')")
	@ApiOperation(
			value = "Module detail",
			nickname = "getModule",
			tags = { CacheController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_READ, description = "") })
				})
	public IdmCacheDto get(
			@ApiParam(value = "Module's identifier.", required = true)
			@PathVariable @NotNull String moduleId) {
		return null;
	}


	/**
	 * Enable module (supports FE and BE  module)
	 *
	 * @param cacheId
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/{cacheId}/evict", method = { RequestMethod.PATCH, RequestMethod.PUT })
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_UPDATE + "')")
	@ApiOperation(
			value = "Evict cache",
			nickname = "evictCache",
			tags = { CacheController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.MODULE_UPDATE, description = "") })
				})
	public void evictCache(
			@ApiParam(value = "Cache identifier.", required = true)
			@PathVariable @NotNull String cacheId) {
		cacheManager.evictCache(cacheId);
	}

}
