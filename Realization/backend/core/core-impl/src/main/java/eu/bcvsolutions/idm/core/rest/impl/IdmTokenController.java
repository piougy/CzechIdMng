package eu.bcvsolutions.idm.core.rest.impl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.CoreModule;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTokenFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmTokenService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.filter.IdmAuthenticationFilter;
import eu.bcvsolutions.idm.core.security.service.impl.JwtAuthenticationMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * IdM tokens.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/tokens")
@Api(
		value = IdmTokenController.TAG, 
		description = "Operations with IdM tokens", 
		tags = { IdmTokenController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmTokenController extends AbstractEventableDtoController<IdmTokenDto, IdmTokenFilter> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmTokenController.class);
	//
	protected static final String TAG = "Tokens";
	//
	@Autowired private EntityEventManager manager;
	@Autowired private JwtAuthenticationMapper jwtTokenMapper;
	
	@Autowired
	public IdmTokenController(IdmTokenService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TOKEN_READ + "')")
	@ApiOperation(
			value = "Search tokens (/search/quick alias)", 
			nickname = "searchTokens",
			tags = { IdmTokenController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TOKEN_READ + "')")
	@ApiOperation(
			value = "Search tokens", 
			nickname = "searchQuickTokens", 
			tags = { IdmTokenController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TOKEN_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countTokens", 
			tags = { IdmTokenController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TOKEN_READ + "')")
	@ApiOperation(
			value = "Token detail", 
			nickname = "getToken", 
			response = IdmTokenDto.class, 
			tags = { IdmTokenController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Token uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	/**
	 * 
	 */
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TOKEN_CREATE + "')")
	@ApiOperation(
			value = "Geerate new token", 
			nickname = "generateToken", 
			response = IdmTokenDto.class, 
			tags = { IdmTokenController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_CREATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_CREATE, description = "")})
				})
	public ResponseEntity<?> post(@RequestBody IdmTokenDto dto) {
		// generate token
		BaseDto owner = getLookupService().lookupDto(dto.getOwnerType(), dto.getOwnerId());
		Assert.notNull(owner, "Owner is required to generate new token.");
		Assert.isInstanceOf(IdmIdentityDto.class, owner, "Identity owner is required to generate new token.");
		IdmIdentityDto identity = (IdmIdentityDto) owner;
		Assert.isTrue(!identity.isDisabled(), MessageFormat.format("Check identity can login: The identity [{0}] is disabled.", identity.getUsername()));
		//
		// set static properties
		dto.setModuleId(CoreModule.MODULE_ID);
		dto.getProperties().put(JwtAuthenticationMapper.PROPERTY_PRESERVE_EXPIRATION, Boolean.TRUE);
		//
		IdmTokenDto token = jwtTokenMapper.createToken(identity, dto);
		IdmJwtAuthenticationDto authenticationDto = jwtTokenMapper.toDto(token);
		//
		// usable token in response after create - only once after create
		// we need to create copy to prevent changes cached token by reference
		IdmTokenDto clone = new IdmTokenDto(token);
		clone.setProperties(new ConfigurationMap(token.getProperties()));
		clone.setDisabled(token.isDisabled());
		clone.setSecretVerified(token.isSecretVerified());
		clone.setExpiration(token.getExpiration());
		clone.setIssuedAt(token.getIssuedAt());
		clone.setTokenType(token.getTokenType());
		clone.setOwnerId(token.getOwnerId());
		clone.setOwnerType(token.getOwnerType());
		clone.setExternalId(token.getExternalId());
		clone.setToken(token.getToken());
		clone.setModuleId(token.getModuleId());
		clone.getProperties().put(
				IdmAuthenticationFilter.AUTHENTICATION_TOKEN_NAME, 
				jwtTokenMapper.writeToken(authenticationDto)
		);
		//
		return new ResponseEntity<>(toResource(clone), HttpStatus.CREATED);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TOKEN_DELETE + "')")
	@ApiOperation(
			value = "Delete token", 
			nickname = "deleteToken", 
			tags = { IdmTokenController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Token uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TOKEN_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnToken", 
			tags = { IdmTokenController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Token uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TOKEN_READ + "')")
	@ApiOperation(
			value = "Get available bulk actions", 
			nickname = "availableBulkAction", 
			tags = { IdmTokenController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_READ, description = "") })
				})
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TOKEN_READ + "')")
	@ApiOperation(
			value = "Process bulk action for token", 
			nickname = "bulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { IdmTokenController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_READ, description = "")})
				})
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TOKEN_READ + "')")
	@ApiOperation(
			value = "Prevalidate bulk action for token", 
			nickname = "prevalidateBulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { IdmTokenController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TOKEN_READ, description = "")})
				})
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	@Override
	public Page<IdmTokenDto> find(IdmTokenFilter filter, Pageable pageable, BasePermission permission) {
		Page<IdmTokenDto> results = super.find(filter, pageable, permission);
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
	protected IdmTokenFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmTokenFilter filter = new IdmTokenFilter(parameters, getParameterConverter());
		// owner decorator
		String ownerId = getParameterConverter().toString(parameters, IdmEntityEventFilter.PARAMETER_OWNER_ID);
		UUID ownerUuid = null;
		String ownerType = filter.getOwnerType();
		if (StringUtils.isNotEmpty(ownerType) && StringUtils.isNotEmpty(ownerId)) {
			// try to find entity owner by Codeable identifier
			AbstractDto owner = manager.findOwner(ownerType, ownerId);
			if (owner != null) {
				ownerUuid = owner.getId();
			} else {
				LOG.debug("Entity type [{}] with identifier [{}] does not found, raw ownerId will be used as uuid.", 
						ownerType, ownerId);
				// Better exception for FE.
				try {
					DtoUtils.toUuid(ownerId);
				} catch (ClassCastException ex) {
					throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", ownerId), ex);
				}
			}
		}
		if (ownerUuid == null) {
			ownerUuid = getParameterConverter().toUuid(parameters, IdmEntityEventFilter.PARAMETER_OWNER_ID);
		}
		filter.setOwnerId(ownerUuid);
		//
		return filter;
	}
}
