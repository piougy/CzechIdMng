package eu.bcvsolutions.idm.core.rest.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Entity states.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/entity-states")
@Api(
		value = IdmEntityStateController.TAG, 
		description = "Operations with entity states", 
		tags = { IdmEntityStateController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmEntityStateController extends AbstractEventableDtoController<IdmEntityStateDto, IdmEntityStateFilter> {
	
	protected static final String TAG = "Entity states";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmEntityStateController.class);
	//
	@Autowired private EntityEventManager manager;
	
	@Autowired
	public IdmEntityStateController(IdmEntityStateService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_READ + "')")
	@ApiOperation(
			value = "Search entity states (/search/quick alias)", 
			nickname = "searchEntityStates", 
			tags = { IdmEntityStateController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_READ + "')")
	@ApiOperation(
			value = "Search entity states", 
			nickname = "searchQuickEntityStates", 
			tags = { IdmEntityStateController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete entity states (selectbox usage)", 
			nickname = "autocompleteEntityStates", 
			tags = { IdmEntityStateController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countEntityStates", 
			tags = { IdmEntityStateController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_READ + "')")
	@ApiOperation(
			value = "EntityState detail", 
			nickname = "getEntityState", 
			response = IdmEntityStateDto.class, 
			tags = { IdmEntityStateController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "EntityState's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ENTITYSTATE_UPDATE + "')")
	@ApiOperation(
			value = "Create / update entity state", 
			nickname = "postEntityState", 
			response = IdmEntityStateDto.class, 
			tags = { IdmEntityStateController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody IdmEntityStateDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_UPDATE + "')")
	@ApiOperation(
			value = "Update entity state", 
			nickname = "putEntityState", 
			response = IdmEntityStateDto.class, 
			tags = { IdmEntityStateController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "EntityState's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmEntityStateDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_UPDATE + "')")
	@ApiOperation(
			value = "Update entity state", 
			nickname = "patchEntityState", 
			response = IdmEntityStateDto.class, 
			tags = { IdmEntityStateController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "EntityState's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_DELETE + "')")
	@ApiOperation(
			value = "Delete entity state", 
			nickname = "deleteEntityState", 
			tags = { IdmEntityStateController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "EntityState's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_READ + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ENTITYSTATE_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnEntityState", 
			tags = { IdmEntityStateController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_READ, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_AUTOCOMPLETE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_READ, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_AUTOCOMPLETE, description = "")})
				})
	public Set<String> getPermissions(
			@ApiParam(value = "EntityState's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	public Page<IdmEntityStateDto> find(IdmEntityStateFilter filter, Pageable pageable, BasePermission permission) {
		Page<IdmEntityStateDto> results = super.find(filter, pageable, permission);
		// fill entity embedded for FE
		Map<UUID, BaseDto> loadedDtos = new HashMap<>();
		results.getContent().forEach(dto -> {
			UUID ownerId = dto.getOwnerId();
			if (!loadedDtos.containsKey(ownerId)) {
				try {
					loadedDtos.put(ownerId, getLookupService().lookupDto(dto.getOwnerType(), ownerId));
				} catch (IllegalArgumentException ex) {
					LOG.debug("Class [{}] not found on classpath (e.g. module was uninstalled)", dto.getOwnerType(), ex);
				}
			}
			dto.getEmbedded().put("ownerId", loadedDtos.get(ownerId));
		});
		return results;
	}

    /**
     * Get available bulk actions for entity state
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_READ + "')")
    @ApiOperation(
            value = "Get available bulk actions",
            nickname = "availableBulkAction",
            tags = {IdmEntityStateController.TAG},
            authorizations = {
                    @Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            @AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_READ, description = "")}),
                    @Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            @AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_READ, description = "")})
            })
    public List<IdmBulkActionDto> getAvailableBulkActions() {
        return super.getAvailableBulkActions();
    }

    /**
     * Process bulk action for entity state
     *
     * @param bulkAction
     * @return
     */
    @ResponseBody
    @RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_READ + "')")
    @ApiOperation(
            value = "Process bulk action for entity state",
            nickname = "bulkAction",
            response = IdmBulkActionDto.class,
            tags = {IdmEntityStateController.TAG},
            authorizations = {
                    @Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            @AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_READ, description = "")}),
                    @Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            @AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_READ, description = "")})
            })
    public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
        return super.bulkAction(bulkAction);
    }

    /**
     * Prevalidate bulk action for entity state
     *
     * @param bulkAction
     * @return
     */
    @ResponseBody
    @RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_READ + "')")
    @ApiOperation(
            value = "Prevalidate bulk action for entity state",
            nickname = "prevalidateBulkAction",
            response = IdmBulkActionDto.class,
            tags = {IdmEntityStateController.TAG},
            authorizations = {
                    @Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            @AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_READ, description = "")}),
                    @Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            @AuthorizationScope(scope = CoreGroupPermission.ENTITYSTATE_READ, description = "")})
            })
    public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
        return super.prevalidateBulkAction(bulkAction);
    }
    
    @Override
	protected IdmEntityStateFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmEntityStateFilter filter = new IdmEntityStateFilter(parameters, getParameterConverter());
		//
		// owner decorator
		String ownerId = getParameterConverter().toString(parameters, IdmEntityStateFilter.PARAMETER_OWNER_ID);
		UUID ownerUuid = null;
		if (StringUtils.isNotEmpty(filter.getOwnerType()) 
				&& StringUtils.isNotEmpty(ownerId)) {
			// try to find entity owner by Codeable identifier
			AbstractDto owner = manager.findOwner(filter.getOwnerType(), ownerId);
			if (owner != null) {
				ownerUuid = owner.getId();
			} else {
				LOG.debug("Entity type [{}] with identifier [{}] does not found, raw ownerId will be used as uuid.", 
						filter.getOwnerType(), ownerId);
				// Better exception for FE.
				try {
					DtoUtils.toUuid(ownerId);
				} catch (ClassCastException ex) {
					throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", ownerId), ex);
				}
			}
		}
		if (ownerUuid == null) {
			ownerUuid = getParameterConverter().toUuid(parameters, "ownerId");
		}
		filter.setOwnerId(ownerUuid);
		//
		return filter;
	}

}
