package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
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
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRemoteServerFilter;
import eu.bcvsolutions.idm.acc.service.api.ConnectorManager;
import eu.bcvsolutions.idm.acc.service.api.SysRemoteServerService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.exception.IcCantConnectException;
import eu.bcvsolutions.idm.ic.exception.IcInvalidCredentialException;
import eu.bcvsolutions.idm.ic.exception.IcRemoteServerException;
import eu.bcvsolutions.idm.ic.exception.IcServerNotFoundException;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationFacade;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;;

/**
 * Remote server with connectors.
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/remote-servers")
@Api(
		value = SysRemoteServerController.TAG, 
		tags = SysRemoteServerController.TAG, 
		description = "Remote server with connectors",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class SysRemoteServerController extends AbstractReadWriteDtoController<SysConnectorServerDto, SysRemoteServerFilter> {

	protected static final String TAG = "Remote servers";
	//
	@Autowired private IcConfigurationFacade icConfiguration;
	@Autowired private ConnectorManager connectorManager;
	//
	private SysRemoteServerService remoteServerService;
	
	@Autowired
	public SysRemoteServerController(SysRemoteServerService remoteServerService) {
		super(remoteServerService);
		//
		this.remoteServerService = remoteServerService;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.REMOTESERVER_READ + "')")
	@ApiOperation(
			value = "Search remote servers (/search/quick alias)", 
			nickname = "searchRemoteServers",
			tags = { SysRemoteServerController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "")})
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.REMOTESERVER_READ + "')")
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search remote servers", 
			nickname = "searchQuickRemoteServers",
			tags = { SysRemoteServerController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "")})
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.REMOTESERVER_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete remote servers (selectbox usage)",
			nickname = "autocompleteRemoteServers",
			tags = { SysRemoteServerController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.REMOTESERVER_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter",
			nickname = "countRemoteServers",
			tags = { SysRemoteServerController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.REMOTESERVER_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@ApiOperation(
			value = "Remote server detail", 
			nickname = "getRemoteServer", 
			response = SysConnectorServerDto.class, 
			tags = { SysRemoteServerController.TAG }, 
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "")})
					})
	public ResponseEntity<?> get(
			@ApiParam(value = "Remote server's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.REMOTESERVER_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.REMOTESERVER_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@ApiOperation(
			value = "Create / update remote server", 
			nickname = "postRemoteServer", 
			response = SysConnectorServerDto.class, 
			tags = { SysRemoteServerController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@RequestBody @NotNull SysConnectorServerDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.REMOTESERVER_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update remote server",
			nickname = "putRemoteServer", 
			response = SysConnectorServerDto.class, 
			tags = { SysRemoteServerController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Remote server's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull SysConnectorServerDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.REMOTESERVER_UPDATE + "')")
	@ApiOperation(
			value = "Patch remote server",
			nickname = "patchRemote server",
			response = SysConnectorServerDto.class,
			tags = { SysRemoteServerController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Remote server uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.REMOTESERVER_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete remote server", 
			nickname = "deleteRemoteServer", 
			tags = { SysRemoteServerController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Remote server's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.REMOTESERVER_READ + "')"
			+ " or hasAuthority('" + AccGroupPermission.REMOTESERVER_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "What logged identity can do with given record",
			nickname = "getPermissionsOnRemoteServer",
			tags = { SysRemoteServerController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_AUTOCOMPLETE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_AUTOCOMPLETE, description = "")})
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Remote server uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.REMOTESERVER_READ + "')")
	@ApiOperation(
			value = "Get available bulk actions",
			nickname = "availableBulkAction",
			tags = { SysRemoteServerController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "") })
				})
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.REMOTESERVER_READ + "')")
	@ApiOperation(
			value = "Process bulk action for remote server",
			nickname = "bulkAction",
			response = IdmBulkActionDto.class,
			tags = { SysRemoteServerController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "")})
				})
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.REMOTESERVER_READ + "')")
	@ApiOperation(
			value = "Prevalidate bulk action for remote server",
			nickname = "prevalidateBulkAction",
			response = IdmBulkActionDto.class,
			tags = { SysRemoteServerController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "")})
				})
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	/**
	 * Return available connector frameworks with connectors on remote connector server.
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}/frameworks")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.REMOTESERVER_READ + "')")
	@ApiOperation(
			value = "Get available connectors",
			nickname = "getAvailableConnectors",
			tags = { SysRemoteServerController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "") })
				},
			notes = "Available connector frameworks with connectors on remote connector server.")
	public ResponseEntity<Map<String, Set<IcConnectorInfo>>> getConnectorFrameworks(
			@ApiParam(value = "Remote server uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		SysConnectorServerDto connectorServer = getDto(backendId);
		if (connectorServer == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		Map<String, Set<IcConnectorInfo>> infos = new HashMap<>();
 		//
 		try {
 			for (IcConfigurationService config: icConfiguration.getIcConfigs().values()) {
 				connectorServer.setPassword(remoteServerService.getPassword(connectorServer.getId()));
				infos.put(config.getFramework(), config.getAvailableRemoteConnectors(connectorServer));
			}
		} catch (IcInvalidCredentialException e) {
			throw new ResultCodeException(AccResultCode.REMOTE_SERVER_INVALID_CREDENTIAL,
					ImmutableMap.of("server", e.getHost() + ":" + e.getPort()), e);
		} catch (IcServerNotFoundException e) {
			throw new ResultCodeException(AccResultCode.REMOTE_SERVER_NOT_FOUND,
					ImmutableMap.of("server", e.getHost() + ":" + e.getPort()), e);
		} catch (IcCantConnectException e) {
			throw new ResultCodeException(AccResultCode.REMOTE_SERVER_CANT_CONNECT,
					ImmutableMap.of("server", e.getHost() + ":" + e.getPort()), e);
		} catch (IcRemoteServerException e) {
			throw new ResultCodeException(AccResultCode.REMOTE_SERVER_UNEXPECTED_ERROR,
					ImmutableMap.of("server", e.getHost() + ":" + e.getPort()), e);
		}
		//
		return new ResponseEntity<Map<String, Set<IcConnectorInfo>>>(infos, HttpStatus.OK);
	}
	
	/**
	 * Returns connector types registered on given remote server.
	 *
	 * @return connector types
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}/connector-types")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.REMOTESERVER_READ + "')")
	@ApiOperation(
			value = "Get supported connector types",
			nickname = "getSupportedConnectorTypes",
			tags = {SysSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "")})
			})
	public Resources<ConnectorTypeDto> getConnectorTypes(
			@ApiParam(value = "Remote server uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		SysConnectorServerDto connectorServer = getDto(backendId);
		if (connectorServer == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
 		//
 		try {
 			List<IcConnectorInfo> connectorInfos = Lists.newArrayList();
 			for (IcConfigurationService config: icConfiguration.getIcConfigs().values()) {
 				connectorServer.setPassword(remoteServerService.getPassword(connectorServer.getId()));
 				Set<IcConnectorInfo> availableRemoteConnectors = config.getAvailableRemoteConnectors(connectorServer);
 				if (CollectionUtils.isNotEmpty(availableRemoteConnectors)) {
 					connectorInfos.addAll(availableRemoteConnectors);
 				}
			}
			// Find connector types for existing connectors.
			List<ConnectorTypeDto> connectorTypes = connectorManager.getSupportedTypes()
					.stream()
					.filter(connectorType -> {
						return connectorInfos.stream()
								.anyMatch(connectorInfo -> connectorType.getConnectorName()
										.equals(connectorInfo.getConnectorKey().getConnectorName()));
					})
					.map(connectorType -> {
						// Find connector info and set version to the connectorTypeDto.
						IcConnectorInfo info = connectorInfos.stream()
								.filter(connectorInfo -> connectorType.getConnectorName()
										.equals(connectorInfo.getConnectorKey().getConnectorName()))
								.findFirst()
								.orElse(null);
						ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
						connectorTypeDto.setLocal(true);
						if (info != null) {
							connectorTypeDto.setVersion(info.getConnectorKey().getBundleVersion());
							connectorTypeDto.setName(info.getConnectorDisplayName());
						}
						return connectorTypeDto;
					})
					.collect(Collectors.toList());

			// Find connectors without extension (specific connector type).
			List<ConnectorTypeDto> defaultConnectorTypes = connectorInfos.stream()
					.map(info -> {
						ConnectorTypeDto connectorTypeDto = connectorManager.convertIcConnectorInfoToDto(info);
						connectorTypeDto.setLocal(true);
						return connectorTypeDto;
					})
					.filter(type -> {
						return !connectorTypes.stream()
								.anyMatch(supportedType ->
										supportedType.getConnectorName().equals(type.getConnectorName()) && supportedType.isHideParentConnector());
					}).collect(Collectors.toList());
			connectorTypes.addAll(defaultConnectorTypes);

			return new Resources<>(
					connectorTypes.stream()
							.sorted(Comparator.comparing(ConnectorTypeDto::getOrder))
							.collect(Collectors.toList())
			);
		} catch (IcInvalidCredentialException e) {
			throw new ResultCodeException(AccResultCode.REMOTE_SERVER_INVALID_CREDENTIAL,
					ImmutableMap.of("server", e.getHost() + ":" + e.getPort()), e);
		} catch (IcServerNotFoundException e) {
			throw new ResultCodeException(AccResultCode.REMOTE_SERVER_NOT_FOUND,
					ImmutableMap.of("server", e.getHost() + ":" + e.getPort()), e);
		} catch (IcCantConnectException e) {
			throw new ResultCodeException(AccResultCode.REMOTE_SERVER_CANT_CONNECT,
					ImmutableMap.of("server", e.getHost() + ":" + e.getPort()), e);
		} catch (IcRemoteServerException e) {
			throw new ResultCodeException(AccResultCode.REMOTE_SERVER_UNEXPECTED_ERROR,
					ImmutableMap.of("server", e.getHost() + ":" + e.getPort()), e);
		}
	}

	@Override
	protected SysRemoteServerFilter toFilter(MultiValueMap<String, Object> parameters) {
		SysRemoteServerFilter filter = new SysRemoteServerFilter(parameters, getParameterConverter());
		filter.setContainsPassword(Boolean.TRUE);
		//
		return filter;
	}
}
