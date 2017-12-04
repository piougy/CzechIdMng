package eu.bcvsolutions.idm.core.rest.impl;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
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
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleAttributeRuleFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Controller of rules for automatic role attribute
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/automatic-role-attribute-rules")
@Api(
		value = IdmAutomaticRoleAttributeRuleController.TAG,  
		tags = { IdmAutomaticRoleAttributeRuleController.TAG }, 
		description = "Rules for automatic role attribute",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmAutomaticRoleAttributeRuleController extends AbstractReadWriteDtoController<IdmAutomaticRoleAttributeRuleDto, IdmAutomaticRoleAttributeRuleFilter>{

	protected static final String TAG = "Rules for automatic role attribute";

	@Autowired
	public IdmAutomaticRoleAttributeRuleController(
			IdmAutomaticRoleAttributeRuleService entityService) {
		super(entityService);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ + "')")
	@ApiOperation(
			value = "Search rules for automatic roles by attribute (/search/quick alias)", 
			nickname = "searchAutomaticRoleAttributeRules", 
			tags = { IdmAutomaticRoleAttributeRuleController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ + "')")
	@ApiOperation(
			value = "Search rules for automatic roles", 
			nickname = "searchQuickAutomaticRoleAttributeRules", 
			tags = { IdmAutomaticRoleAttributeRuleController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ + "')")
	@ApiOperation(
			value = "Rule detail", 
			nickname = "getAutomaticRoleAttributeRule", 
			response = IdmAutomaticRoleAttributeRuleDto.class, 
			tags = { IdmAutomaticRoleAttributeRuleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Rule's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ + "')"
			+ " or hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_UPDATE + "')")
	@ApiOperation(
			value = "Create / update rule", 
			nickname = "postAutomaticRoleAttributeRule", 
			response = IdmAutomaticRoleAttributeRuleDto.class, 
			tags = { IdmAutomaticRoleAttributeRuleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_READ, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody IdmAutomaticRoleAttributeRuleDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_DELETE + "')")
	@ApiOperation(
			value = "Delete rule", 
			nickname = "deleteAutomaticRoleAttributeRule", 
			tags = { IdmAutomaticRoleAttributeRuleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUTOMATIC_ROLE_ATTRIBUTE_RULE_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Rule's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
}
