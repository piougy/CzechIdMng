package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysConnectorServer;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemFormValue;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteEntityController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.domain.IcResultCode;
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
 * Target system setting controller
 * 
 * TODO: remove ROLE_READ access? 
 * 
 * @author Radek Tomiška
 * @author Vít Švanda
 * @author Ondřej Kopr
 *
 */
@RepositoryRestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseEntityController.BASE_PATH + "/systems")
@Api(
		value = SysSystemController.TAG, 
		tags = SysSystemController.TAG, 
		description = "Operations with target systems",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class SysSystemController extends AbstractReadWriteEntityController<SysSystem, SysSystemFilter> {
	
	protected static final String TAG = "Systems";
	//
	private final SysSystemService systemService;
	private final IcConfigurationFacade icConfiguration;
	private final ConfidentialStorage confidentialStorage;
	//
	private final IdmFormDefinitionController formDefinitionController;
	
	@Autowired
	public SysSystemController(
			LookupService entityLookupService, 
			SysSystemService systemService, 
			IdmFormDefinitionController formDefinitionController,
			IcConfigurationFacade icConfiguration,
			ConfidentialStorage confidentialStorage) {
		super(entityLookupService);
		//
		Assert.notNull(systemService);
		Assert.notNull(formDefinitionController);
		Assert.notNull(icConfiguration);
		Assert.notNull(confidentialStorage);
		//
		this.systemService = systemService;
		this.formDefinitionController = formDefinitionController;
		this.icConfiguration = icConfiguration;
		this.confidentialStorage = confidentialStorage;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@ApiOperation(
			value = "Search systems (/search/quick alias)", 
			nickname = "searchSystems",
			tags = { SysSystemController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")})
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable,
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search systems", 
			nickname = "searchQuickSystems",
			tags = { SysSystemController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")})
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable, 
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@ApiOperation(
			value = "System detail", 
			nickname = "getSystem", 
			response = SysSystem.class, 
			tags = { SysSystemController.TAG }, 
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")})
					})
	public ResponseEntity<?> get(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, 
			PersistentEntityResourceAssembler assembler) {
		return super.get(backendId, assembler);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@ApiOperation(
			value = "Create / update system", 
			nickname = "postSystem", 
			response = SysSystem.class, 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.post(nativeRequest, assembler);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update system",
			nickname = "putSystem", 
			response = SysSystem.class, 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, 
			HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.put(backendId, nativeRequest, assembler);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@ApiOperation(
			value = "Update system",
			nickname = "patchSystem", 
			response = SysSystem.class, 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, 
			HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete system", 
			nickname = "deleteSystem", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/generate-schema", method = RequestMethod.POST)
	@ApiOperation(
			value = "Generate system schema", 
			nickname = "generateSystemSchema", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") })
				},
			notes = "Genetares schema by system's connector configuration")
	public ResponseEntity<?> generateSchema(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, 
			PersistentEntityResourceAssembler assembler) {
		SysSystem system = getEntity(backendId);
		if (system == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		systemService.generateSchema(system);
		return new ResponseEntity<>(toResource(system, assembler), HttpStatus.OK);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/duplicate", method = RequestMethod.POST)
	@ApiOperation(
			value = "Create system duplicate (copy)", 
			nickname = "duplicateSystem", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") })
				},
			notes = "Creates system duplicate with all configurations - connector, schemas, mappings etc.. Duplicate is disabled by default.")
	public ResponseEntity<?> duplicate(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, 
			PersistentEntityResourceAssembler assembler) {
		SysSystem system = getEntity(backendId);
		if (system == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		SysSystem duplicate = systemService.duplicate(system.getId());
		return new ResponseEntity<>(toResource(duplicate, assembler), HttpStatus.OK);
	}

	@Override
	protected SysSystemFilter toFilter(MultiValueMap<String, Object> parameters) {
		SysSystemFilter filter = new SysSystemFilter();
		filter.setText((String) parameters.toSingleValueMap().get("text"));
		// TODO: diff between validate and generate policy
		filter.setPasswordPolicyValidationId(getParameterConverter().toUuid(parameters, "passwordPolicyId"));

		return filter;
	}
	
	/**
	 * Test usage only
	 * 
	 * @return
	 */
	@Deprecated
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@RequestMapping(value = "/test/create-test-system", method = RequestMethod.POST)
	@ApiOperation(
			value = "Create test system", 
			nickname = "createTestSystem", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = IdmGroupPermission.APP_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = IdmGroupPermission.APP_ADMIN, description = "") })
				},
			notes = "Creates system with test connector configuration - usign local table \"system_users\".")
	public ResponseEntity<?> createTestSystem() {
		systemService.createTestSystem();
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}	
	
	/**
	 * Returns connector form definition to given system
	 * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/connector-form-definition", method = RequestMethod.GET)
	@ApiOperation(
			value = "Connector configuration - form definition", 
			nickname = "getConnectorFormDefinition", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") })
				})
	public ResponseEntity<?> getConnectorFormDefinition(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, 
			PersistentEntityResourceAssembler assembler) {
		SysSystem system = getEntity(backendId);
		if (system == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinition formDefinition = getConnectorFormDefinition(system);
		return formDefinitionController.get(formDefinition.getId().toString(), assembler);	
	}
	
	/**
	 * Returns filled connector configuration
	 * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/connector-form-values", method = RequestMethod.GET)
	@ApiOperation(
			value = "Connector configuration - read values", 
			nickname = "getConnectorFormValues", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") })
				})
	public Resources<?> getConnectorFormValues(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, 
			PersistentEntityResourceAssembler assembler) {
		SysSystem entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinition formDefinition = getConnectorFormDefinition(entity);
		return formDefinitionController.getFormValues(entity, formDefinition, assembler);
	}
	
	/**
	 * Saves connector configuration form values
	 * 
	 * @param backendId
	 * @param formValues
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/connector-form-values", method = RequestMethod.POST)
	@ApiOperation(
			value = "Connector configuration - save values", 
			nickname = "postConnectorFormValues", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") })
				})
	public Resources<?> saveConnectorFormValues(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @Valid List<SysSystemFormValue> formValues,
			PersistentEntityResourceAssembler assembler) {		
		SysSystem entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinition formDefinition = getConnectorFormDefinition(entity);
		return formDefinitionController.saveFormValues(entity, formDefinition, formValues, assembler);
	}
	
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/check", method = RequestMethod.GET)
	@ApiOperation(
			value = "Check system", 
			nickname = "checkSystem", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") })
				},
			notes = "Check system connector configuration.")
	public ResponseEntity<?> checkSystem(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId,
			PersistentEntityResourceAssembler assembler) {
		systemService.checkSystem(super.getEntity(backendId));
		return new ResponseEntity<>(true, HttpStatus.OK);
	}
	
	/**
	 * Return all local connectors of given framework
	 * 
	 * @param framework - ic framework
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/search/local")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@ApiOperation(
			value = "Get available local connectors", 
			nickname = "getAvailableLocalConnectors", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") })
				},
			notes = "Supported local conectors (on classpath).")
	public ResponseEntity<Map<String, List<IcConnectorInfo>>> getAvailableLocalConnectors(
			@ApiParam(value = "Connector framework.", example = "connId", defaultValue = "connId")
			@RequestParam(required = false) String framework) {
		Map<String, List<IcConnectorInfo>> infos = new HashMap<>();
		if (framework != null) {
			if (!icConfiguration.getIcConfigs().containsKey(framework)) {
				throw new ResultCodeException(IcResultCode.IC_FRAMEWORK_NOT_FOUND,
						ImmutableMap.of("framework", framework));
			}
			infos.put(framework, icConfiguration.getIcConfigs().get(framework)
					.getAvailableLocalConnectors());

		} else {
			infos = icConfiguration.getAvailableLocalConnectors();
		}
		return new ResponseEntity<Map<String, List<IcConnectorInfo>>>(infos, HttpStatus.OK);
	}
	
	/**
	 * Rest endpoints return available remote connectors.
	 * If entity hasn't set for remote or isn't exists return empty map of connectors
	 * 
	 * @param backendId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "{backendId}/search/remote")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@ApiOperation(
			value = "Get available remote connectors", 
			nickname = "getAvailableRemoteConnectors", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") })
				},
			notes = "Supported remote conectors (by remote server configuration).")
	public ResponseEntity<Map<String, List<IcConnectorInfo>>> getAvailableRemoteConnectors(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		SysSystem entity = this.getEntity(backendId);

		Map<String, List<IcConnectorInfo>> infos = new HashMap<>();
		
		// if entity hasn't set up for remote return empty map
		if (entity == null || !entity.isRemote()) {
			return new ResponseEntity<Map<String, List<IcConnectorInfo>>>(infos, HttpStatus.OK);
		}

 		Assert.notNull(entity.getConnectorServer());
 		//
 		try {
 			for (IcConfigurationService config: icConfiguration.getIcConfigs().values()) {
				SysConnectorServer server = entity.getConnectorServer();
				server.setPassword(this.confidentialStorage.getGuardedString(entity.getId(), getEntityClass(), SysSystemService.REMOTE_SERVER_PASSWORD));
				infos.put(config.getFramework(), config.getAvailableRemoteConnectors(server));
			}
		} catch (IcInvalidCredentialException e) {
			throw new ResultCodeException(AccResultCode.REMOTE_SERVER_INVALID_CREDENTIAL, ImmutableMap.of("server", e.getHost() + ":" + e.getPort()));
		} catch (IcServerNotFoundException e) {
			throw new ResultCodeException(AccResultCode.REMOTE_SERVER_NOT_FOUND, ImmutableMap.of("server", e.getHost() + ":" + e.getPort()));
		} catch (IcCantConnectException e) {
			throw new ResultCodeException(AccResultCode.REMOTE_SERVER_CANT_CONNECT, ImmutableMap.of("server", e.getHost() + ":" + e.getPort()));
		} catch (IcRemoteServerException e) {
			throw new ResultCodeException(AccResultCode.REMOTE_SERVER_UNEXPECTED_ERROR, ImmutableMap.of("server", e.getHost() + ":" + e.getPort()));
		}
		//
		return new ResponseEntity<Map<String, List<IcConnectorInfo>>>(infos, HttpStatus.OK);
	}
	
	/**
	 * Returns definition for given system 
	 * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
	 * 
	 * @param system
	 * @return
	 */
	private synchronized IdmFormDefinition getConnectorFormDefinition(SysSystem system) {
		Assert.notNull(system);
		//
		// connector key can't be null
		if (system.getConnectorKey() == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_FORM_DEFINITION_NOT_FOUND, ImmutableMap.of("system", system.getId()));
		}
		// for remote connector form definition we need password for remote connector server
		if (system.isRemote()) {
			SysConnectorServer connectorServer = system.getConnectorServer();
			connectorServer.setPassword(this.confidentialStorage.getGuardedString(system.getId(), getEntityClass(), SysSystemService.REMOTE_SERVER_PASSWORD));
			system.setConnectorServer(connectorServer);
		}
		//
		return systemService.getConnectorFormDefinition(system.getConnectorInstance());
	}
}
