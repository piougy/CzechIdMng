package eu.bcvsolutions.idm.core.rest.impl;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Default controller for scripts, basic methods.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/scripts")
@Api(
		value = IdmScriptController.TAG,  
		tags = { IdmScriptController.TAG }, 
		description = "Groovy scripts administration",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmScriptController extends AbstractReadWriteDtoController<IdmScriptDto, IdmScriptFilter> {
	
	protected static final String TAG = "Scripts";
	private final IdmScriptService service;
	
	@Autowired
	public IdmScriptController(IdmScriptService service) {
		super(service);
		//
		this.service = service;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@ApiOperation(
			value = "Search scripts (/search/quick alias)", 
			nickname = "searchScripts", 
			tags = { IdmScriptController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@ApiOperation(
			value = "Search scripts", 
			nickname = "searchQuickScripts", 
			tags = { IdmScriptController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_AUTOCOMPLETE + "')")
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@ApiOperation(
			value = "Autocomplete scripts (selectbox usage)", 
			nickname = "autocompleteScripts", 
			tags = { IdmScriptController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@ApiOperation(
			value = "Script detail", 
			nickname = "getScript", 
			response = IdmScriptDto.class, 
			tags = { IdmScriptController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Script's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.SCRIPT_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@ApiOperation(
			value = "Create / update script", 
			nickname = "postScript", 
			response = IdmScriptDto.class, 
			tags = { IdmScriptController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@RequestBody @NotNull IdmScriptDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_UPDATE + "')")
	@ApiOperation(
			value = "Update script", 
			nickname = "putScript", 
			response = IdmScriptDto.class, 
			tags = { IdmScriptController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Script's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull IdmScriptDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete script", 
			nickname = "deleteScript", 
			tags = { IdmScriptController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Script's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/redeploy", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@ApiOperation(
			value = "Redeploy script", 
			nickname = "redeployScript", 
			response = IdmScriptDto.class, 
			tags = { IdmScriptController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_READ, description = "") })
				},
			notes = "Redeploy script. Redeployed will be only scripts, that has pattern in resource."
					+ " Before save newly loaded DO will be backup the old script into backup directory.")
	public ResponseEntity<?> redeploy(
			@ApiParam(value = "Script's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmScriptDto script = service.get(backendId);
		if (script == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, backendId);
		}
		script = service.redeploy(script);
		return new ResponseEntity<>(toResource(script), HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/backup", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@ApiOperation(
			value = "Backup script", 
			nickname = "backupScript", 
			response = IdmScriptDto.class, 
			tags = { IdmScriptController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_READ, description = "") })
				},
			notes = "Backup template to directory given in application properties.")
	public ResponseEntity<?> backup(
			@ApiParam(value = "Script's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmScriptDto script = service.get(backendId);
		if (script == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, backendId);
		}
		service.backup(script);
		return new ResponseEntity<>(toResource(script), HttpStatus.OK);
	}
	
	@Override
	protected IdmScriptFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmScriptFilter filter = new IdmScriptFilter(parameters);
		filter.setDescription(getParameterConverter().toString(parameters, "description"));
		filter.setCode(getParameterConverter().toString(parameters, "code"));
		filter.setUsedIn(getParameterConverter().toString(parameters, "usedIn"));
		filter.setCategory(getParameterConverter().toEnum(parameters, "category", IdmScriptCategory.class));
		filter.setInCategory(getParameterConverter().toEnums(parameters, "inCategory", IdmScriptCategory.class));
		//
		return filter;
	}
}
