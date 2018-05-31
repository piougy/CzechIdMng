package eu.bcvsolutions.idm.core.rest.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Configuration controller - add custom methods to configuration repository
 * 
 * @author Radek Tomiška 
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/configurations")
@Api(
		value = IdmConfigurationController.TAG, 
		description = "Application configuration", 
		tags = { IdmConfigurationController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmConfigurationController extends AbstractReadWriteDtoController<IdmConfigurationDto, DataFilter> {
	
	protected static final String TAG = "Configuration";
	private final IdmConfigurationService configurationService;
	
	@Autowired
	public IdmConfigurationController(IdmConfigurationService configurationService) {
		super(configurationService);
		//
		this.configurationService = configurationService;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_READ + "')")
	@ApiOperation(
			value = "Search configuration items (/search/quick alias)", 
			nickname = "searchConfigurations", 
			tags = { IdmConfigurationController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_READ + "')")
	@ApiOperation(
			value = "Search configuration items", 
			nickname = "searchQuickConfigurations", 
			tags = { IdmConfigurationController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countConfigurations", 
			tags = { IdmConfigurationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_READ + "')")
	@ApiOperation(
			value = "Configuration item detail", 
			nickname = "getConfiguration", 
			response = IdmConfigurationDto.class, 
			tags = { IdmConfigurationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Item's uuid identifier or name (=> code).", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.CONFIGURATION_UPDATE + "')")
	@ApiOperation(
			value = "Create / update configuration item", 
			nickname = "postConfiguration", 
			response = IdmConfigurationDto.class, 
			tags = { IdmConfigurationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody IdmConfigurationDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_UPDATE + "')")
	@ApiOperation(
			value = "Update configuration item", 
			nickname = "putConfiguration", 
			response = IdmConfigurationDto.class, 
			tags = { IdmConfigurationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Item's uuid identifier or name (=> code).", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmConfigurationDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_DELETE + "')")
	@ApiOperation(
			value = "Delete configuration item", 
			nickname = "deleteConfiguration", 
			tags = { IdmConfigurationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Item's uuid identifier or name (=> code).", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnConfiguration", 
			tags = { IdmIdentityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Item's uuid identifier or name (=> code).", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	/**
	 * Returns configurations from property files 
	 * 
	 * @return
	 */
	@ResponseBody
	@PostFilter("hasAuthority('" + CoreGroupPermission.CONFIGURATION_READ + "')")
	@RequestMapping(path = "/all/file", method = RequestMethod.GET)
	@ApiOperation(
			value = "Get all configuration items from files", 
			nickname = "getAllConfigurationsFromFiles", 
			tags = { IdmConfigurationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_READ, description = "") })
				},
			notes = "E.g. from application.properties, module-*.properties etc.")
	public List<IdmConfigurationDto> getAllConfigurationsFromFiles() {
		// TODO: resource wrapper + assembler
		return configurationService.getAllConfigurationsFromFiles();
	}
	
	/**
	 * Returns configurations from property files 
	 * 
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONFIGURATION_ADMIN + "')")
	@RequestMapping(path = "/all/environment", method = RequestMethod.GET)
	@ApiOperation(
			value = "Get all configuration items from environment", 
			nickname = "getAllConfigurationsFromEnvironment", 
			tags = { IdmConfigurationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_ADMIN, description = "") })
				},
			notes = "Server environment properties.")
	public List<IdmConfigurationDto> getAllConfigurationsFromEnvironment() {
		// TODO: resource wrapper + assembler + hateoas links
		return configurationService.getAllConfigurationsFromEnvironment();
	}
	
	/**
	 * Bulk configuration save
	 * 
	 * @param configuration
	 * @return
	 * @throws IOException 
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@RequestMapping(value = "/bulk/save", method = RequestMethod.PUT, consumes = MediaType.TEXT_PLAIN_VALUE, produces = BaseController.APPLICATION_HAL_JSON_VALUE)
	@ApiOperation(
			value = "Save configuration items in bulk", 
			nickname = "saveConfigurationBulk", 
			tags = { IdmConfigurationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.CONFIGURATION_ADMIN, description = "") })
				},
			notes = "Save configuration properties pasted from configration file (e.q. from application.properties)."
					+ " Simple text/plain .properties format is accepted.")
	public void saveProperties(@RequestBody String configuration) {
		try {
			Properties p = new Properties();
	    	p.load(new StringReader(configuration));
	    	p.forEach((name, value) -> {
	    		configurationService.setValue(name.toString(), value == null ? null : value.toString().split("#")[0].trim());
	    	});
		} catch (IOException ex) {
			throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
		} catch (IllegalArgumentException ex) {
			throw new ResultCodeException(CoreResultCode.BAD_REQUEST, ex.getLocalizedMessage());
		}
	}

	@Override
	protected DataFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new DataFilter(getDtoClass(), parameters);
	}
}
