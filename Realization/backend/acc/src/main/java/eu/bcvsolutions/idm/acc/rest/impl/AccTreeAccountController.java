package eu.bcvsolutions.idm.acc.rest.impl;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccRoleAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccTreeAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.TreeAccountFilter;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.rest.impl.DefaultReadWriteDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Tree node accounts on target system
 *
 * @author Kučera
 *
 */

@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/tree-accounts")
@Api(
		value = AccTreeAccountController.TAG,
		tags = { AccTreeAccountController.TAG },
		description = "Assigned tree node accounts on target system",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class AccTreeAccountController extends DefaultReadWriteDtoController<AccTreeAccountDto, TreeAccountFilter> {
	protected static final String TAG = "Tree accounts";

	@Autowired
	public AccTreeAccountController(ReadWriteDtoService<AccTreeAccountDto, TreeAccountFilter> service) {
		super(service);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.TREE_ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Tree node account detail",
			nickname = "getTreeNodeAccount",
			response = AccTreeAccountDto.class,
			tags = { AccTreeAccountController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.TREE_ACCOUNT_READ, description = "")	}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.TREE_ACCOUNT_READ, description = "")	})
			})
	public ResponseEntity<?> get(
			@ApiParam(value = "Tree node account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.TREE_ACCOUNT_CREATE + "')"
			+ "or hasAuthority('" + AccGroupPermission.TREE_ACCOUNT_UPDATE + "')")
	@ApiOperation(
			value = "Create / update tree node account",
			nickname = "postTreeNodeAccount",
			response = AccTreeAccountDto.class,
			tags = { AccTreeAccountController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.TREE_ACCOUNT_CREATE, description = ""),
							@AuthorizationScope(scope = AccGroupPermission.TREE_ACCOUNT_UPDATE, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.TREE_ACCOUNT_CREATE, description = ""),
							@AuthorizationScope(scope = AccGroupPermission.TREE_ACCOUNT_UPDATE, description = "")})
			})
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> post(@RequestBody @NotNull AccTreeAccountDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.TREE_ACCOUNT_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update tree node account",
			nickname = "putTreeNodeAccount",
			response = AccRoleAccountDto.class,
			tags = { AccTreeAccountController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.TREE_ACCOUNT_UPDATE, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.TREE_ACCOUNT_UPDATE, description = "")})
			})
	public ResponseEntity<?> put(
			@ApiParam(value = "Tree node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull AccTreeAccountDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.TREE_ACCOUNT_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete tree node account",
			nickname = "deleteTreeNodeAccount",
			tags = { AccTreeAccountController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.TREE_ACCOUNT_DELETE, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.TREE_ACCOUNT_DELETE, description = "")})
			})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Tree node's account uuid identifier", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	protected TreeAccountFilter toFilter(MultiValueMap<String, Object> parameters) {
		TreeAccountFilter filter = new TreeAccountFilter();
		filter.setAccountId(getParameterConverter().toUuid(parameters, "accountId"));
		filter.setRoleSystemId(getParameterConverter().toUuid(parameters, ""));
		filter.setTreeNodeId(getParameterConverter().toEntityUuid(parameters, "tree-nodes", IdmTreeNode.class));
		filter.setSystemId(getParameterConverter().toUuid(parameters, "systemId"));
		filter.setOwnership(getParameterConverter().toBoolean(parameters, "ownership"));
		return filter;
	}
}
