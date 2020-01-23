package eu.bcvsolutions.idm.core.rest.impl;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmCacheDto;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

import static eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission.APP_ADMIN;

/**
 * Cache controller - provides clear cache and search all functionality
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
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
	@Autowired private PagedResourcesAssembler<IdmCacheDto> pagedResourcesAssembler;

	/**
	 * Returns all available caches. Uses {@link IdmCacheManager} to fetch them. No filtering is possible. This method
	 * always returns all currently created caches. Note that spring creates caches lazily, so not all expected caches
	 * may be present in the result set.
	 *
	 * @return a list of {@link IdmCacheDto}
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + APP_ADMIN + "')")
	@ApiOperation(
			value = "Get all available caches",
			nickname = "getAvailableCaches",
			tags = { CacheController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = APP_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = APP_ADMIN, description = "") })
				})
	public PagedResources<Resource<IdmCacheDto>> getAvailableCaches() {
		return pagedResourcesAssembler.toResource(cacheManager.getAllAvailableCaches());
	}


	/**
	 * Evict cache with given name. If cache with given name does not exist, then {@link eu.bcvsolutions.idm.core.api.exception.ResultCodeException}
	 * will be thrown. Note that caches may be created lazily, so your cache may not be created in the time of calling
	 * this method. To avoid errors, check that your cache is created using getAvailableCaches.
	 *
	 * @param cacheId Name of cache to evict
	 * @throws eu.bcvsolutions.idm.core.api.exception.ResultCodeException If cache with given name does not exist
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/{cacheId}/evict", method = { RequestMethod.PATCH, RequestMethod.PUT })
	@PreAuthorize("hasAuthority('" + APP_ADMIN + "')")
	@ApiOperation(
			value = "Evict cache",
			nickname = "evictCache",
			tags = { CacheController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = APP_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = APP_ADMIN, description = "") })
				})
	public void evictCache(
			@ApiParam(value = "Cache identifier.", required = true)
			@PathVariable @NotNull String cacheId) {
		cacheManager.evictCache(cacheId);
	}

	/**
	 * Evict all caches. It evicts all caches, that are present in container at the time of calling this method.
	 *
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/evict", method = { RequestMethod.PATCH, RequestMethod.PUT })
	@PreAuthorize("hasAuthority('" + APP_ADMIN + "')")
	@ApiOperation(
			value = "Evict all caches",
			nickname = "evictAllCaches",
			tags = { CacheController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = APP_ADMIN, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = APP_ADMIN, description = "") })
			})
	public void evictAllCaches() {
		cacheManager.evictAllCaches();
	}

}
