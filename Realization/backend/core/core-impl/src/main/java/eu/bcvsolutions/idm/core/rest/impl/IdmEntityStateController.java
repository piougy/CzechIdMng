package eu.bcvsolutions.idm.core.rest.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Entity states
 * 
 * @author Radek Tomiška
 *
 */
@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/entity-states")
@Api(
		value = IdmEntityStateController.TAG, 
		description = "Operations with entity states", 
		tags = { IdmEntityStateController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmEntityStateController extends DefaultReadWriteDtoController<IdmEntityStateDto, IdmEntityStateFilter> {
	
	protected static final String TAG = "Entity states";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmEntityStateController.class);
	//
	@Autowired private EntityEventManager manager;
	
	@Autowired
	public IdmEntityStateController(IdmEntityStateService service) {
		super(service);
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
	
	@Override
	protected IdmEntityStateFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmEntityStateFilter filter = new IdmEntityStateFilter(parameters);
		filter.setCreatedFrom(getParameterConverter().toDateTime(parameters, "createdFrom"));
		filter.setCreatedTill(getParameterConverter().toDateTime(parameters, "createdTill"));
		filter.setOwnerType(getParameterConverter().toString(parameters, "ownerType"));
		filter.setResultCode(getParameterConverter().toString(parameters, "resultCode"));
		filter.setOperationStates(getParameterConverter().toEnums(parameters, "operationStates", OperationState.class));
		//
		String ownerId = getParameterConverter().toString(parameters, "ownerId");
		if (StringUtils.isNotEmpty(filter.getOwnerType()) 
				&& StringUtils.isNotEmpty(ownerId)) {
			// try to find entity owner by Codeable identifier
			AbstractDto owner = manager.findOwner(filter.getOwnerType(), ownerId);
			if (owner != null) {
				filter.setOwnerId(owner.getId());
			} else {
				throw new ResultCodeException(CoreResultCode.BAD_VALUE, "Entity type [%s] with identifier [%s] does not found",
						ImmutableMap.of("entityClass", filter.getOwnerType(), "identifier", ownerId));
			}
		} else {
			try {
				UUID uuid = getParameterConverter().toUuid(parameters, "ownerId");
				filter.setOwnerId(uuid);
			} catch (ClassCastException ex) {
				throw new ResultCodeException(CoreResultCode.BAD_FILTER, ex);
			}
		}
		filter.setEventId(getParameterConverter().toUuid(parameters, "eventId"));
		return filter;
	}

    /**
     * Get available bulk actions for entity state
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
    @ApiOperation(
            value = "Get available bulk actions",
            nickname = "availableBulkAction",
            tags = {IdmEntityStateController.TAG},
            authorizations = {
                    @Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            @AuthorizationScope(scope = IdmGroupPermission.APP_ADMIN, description = "")}),
                    @Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            @AuthorizationScope(scope = IdmGroupPermission.APP_ADMIN, description = "")})
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
    @PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
    @ApiOperation(
            value = "Process bulk action for entity state",
            nickname = "bulkAction",
            response = IdmBulkActionDto.class,
            tags = {IdmEntityStateController.TAG},
            authorizations = {
                    @Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            @AuthorizationScope(scope = IdmGroupPermission.APP_ADMIN, description = "")}),
                    @Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            @AuthorizationScope(scope = IdmGroupPermission.APP_ADMIN, description = "")})
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
    @PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
    @ApiOperation(
            value = "Prevalidate bulk action for entity state",
            nickname = "prevalidateBulkAction",
            response = IdmBulkActionDto.class,
            tags = {IdmEntityStateController.TAG},
            authorizations = {
                    @Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            @AuthorizationScope(scope = IdmGroupPermission.APP_ADMIN, description = "")}),
                    @Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            @AuthorizationScope(scope = IdmGroupPermission.APP_ADMIN, description = "")})
            })
    public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
        return super.prevalidateBulkAction(bulkAction);
    }

}
