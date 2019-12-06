package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.scheduling.annotation.Scheduled;
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
import org.springframework.web.context.request.async.DeferredResult;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncItemLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.model.service.api.CheckLongPollingResult;
import eu.bcvsolutions.idm.core.model.service.api.LongPollingManager;
import eu.bcvsolutions.idm.core.rest.DeferredResultWrapper;
import eu.bcvsolutions.idm.core.rest.LongPollingSubscriber;
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
 * @author Radek Tomiška
 * @author Vít Švanda
 * @author Ondřej Kopr
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/systems")
@Api(
		value = SysSystemController.TAG, 
		tags = SysSystemController.TAG, 
		description = "Operations with target systems",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class SysSystemController extends AbstractReadWriteDtoController<SysSystemDto, SysSystemFilter> {
	
	protected static final String TAG = "Systems";
	//
	private final SysSystemService systemService;
	private final IcConfigurationFacade icConfiguration;
	private final ConfidentialStorage confidentialStorage;
	//
	private final IdmFormDefinitionController formDefinitionController;
	@Autowired
	private SysSyncItemLogService syncItemLogService;
	@Autowired
	private SysSyncLogService syncLogService;
	@Autowired
	private LongPollingManager longPollingManager;
	
	@Autowired
	public SysSystemController(
			SysSystemService systemService, 
			IdmFormDefinitionController formDefinitionController,
			IcConfigurationFacade icConfiguration,
			ConfidentialStorage confidentialStorage) {
		super(systemService);
		//
		Assert.notNull(systemService, "Service is required.");
		Assert.notNull(formDefinitionController, "Controller is required.");
		Assert.notNull(icConfiguration, "Configuration is required.");
		Assert.notNull(confidentialStorage, "Confidential storage is required.");
		//
		this.systemService = systemService;
		this.formDefinitionController = formDefinitionController;
		this.icConfiguration = icConfiguration;
		this.confidentialStorage = confidentialStorage;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@ApiOperation(
			value = "Search systems (/search/quick alias)", 
			nickname = "searchSystems",
			tags = { SysSystemController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")})
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search systems", 
			nickname = "searchQuickSystems",
			tags = { SysSystemController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")})
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete systems (selectbox usage)", 
			nickname = "autocompleteSystems", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countSystems", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@ApiOperation(
			value = "System detail", 
			nickname = "getSystem", 
			response = SysSystemDto.class, 
			tags = { SysSystemController.TAG }, 
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")})
					})
	public ResponseEntity<?> get(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@ApiOperation(
			value = "Create / update system", 
			nickname = "postSystem", 
			response = SysSystemDto.class, 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@RequestBody @NotNull SysSystemDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update system",
			nickname = "putSystem", 
			response = SysSystemDto.class, 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, @RequestBody @NotNull SysSystemDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@ApiOperation(
			value = "Patch system", 
			nickname = "patchSystem", 
			response = SysSystemDto.class, 
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
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
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
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')"
			+ " or hasAuthority('" + AccGroupPermission.SYSTEM_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnSystem", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_AUTOCOMPLETE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_AUTOCOMPLETE, description = "")})
				})
	public Set<String> getPermissions(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
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
			@PathVariable @NotNull String backendId) {
		SysSystemDto system = getDto(backendId);
		if (system == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		systemService.generateSchema(system);
		return new ResponseEntity<>(toResource(system), HttpStatus.OK);
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
			@PathVariable @NotNull String backendId) {
		SysSystemDto system = getDto(backendId);
		if (system == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		SysSystemDto duplicate = systemService.duplicate(system.getId());
		return new ResponseEntity<>(toResource(duplicate), HttpStatus.OK);
	}
	
	/**
	 * Test usage only
	 * 
	 * @return
	 */
	@Deprecated
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
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
			@PathVariable @NotNull String backendId) {
		SysSystemDto system = getDto(backendId);
		if (system == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinitionDto formDefinition = getConnectorFormDefinition(system);
		//
		return new ResponseEntity<>(new Resource<>(formDefinition), HttpStatus.OK);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/pooling-connector-form-definition", method = RequestMethod.GET)
	@ApiOperation(
			value = "Pooling connector configuration - form definition", 
			nickname = "getPoolingConnectorFormDefinition", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") })
				})
	public ResponseEntity<?> getPoolingConnectorFormDefinition(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		SysSystemDto system = getDto(backendId);
		if (system == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinitionDto formDefinition = getPoolingConnectorFormDefinition(system);
		//
		return new ResponseEntity<>(new Resource<>(formDefinition), HttpStatus.OK);
	}
	
	
	
	/**
	 * Returns filled connector configuration
	 * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
	 * 
	 * @param backendId
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
	public Resource<?> getConnectorFormValues(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		SysSystemDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinitionDto formDefinition = getConnectorFormDefinition(entity);
		return formDefinitionController.getFormValues(entity, formDefinition);
	}
	
	
	/**
	 * Returns filled pooling connector configuration
	 * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/pooling-connector-form-values", method = RequestMethod.GET)
	@ApiOperation(
			value = "Connector configuration - read values", 
			nickname = "getPoolingConnectorFormValues", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") })
				})
	public Resource<?> getPoolingConnectorFormValues(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		SysSystemDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinitionDto formDefinition = getPoolingConnectorFormDefinition(entity);
		return formDefinitionController.getFormValues(entity, formDefinition);
	}
	
	
	
	/**
	 * Saves connector configuration form values
	 * 
	 * @param backendId
	 * @param formValues
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
	public Resource<?> saveConnectorFormValues(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @Valid List<IdmFormValueDto> formValues) {		
		SysSystemDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinitionDto formDefinition = getConnectorFormDefinition(entity);
		return formDefinitionController.saveFormValues(entity, formDefinition, formValues);
	}
	
	

	/**
	 * Saves pooling connector configuration form values
	 * 
	 * @param backendId
	 * @param formValues
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/pooling-connector-form-values", method = RequestMethod.POST)
	@ApiOperation(
			value = "Pooling connector configuration - save values", 
			nickname = "postPoolingConnectorFormValues", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") })
				})
	public Resource<?> savePoolingConnectorFormValues(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @Valid List<IdmFormValueDto> formValues) {		
		SysSystemDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinitionDto formDefinition = getPoolingConnectorFormDefinition(entity);
		return formDefinitionController.saveFormValues(entity, formDefinition, formValues);
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
			@PathVariable @NotNull String backendId) {
		systemService.checkSystem(super.getDto(backendId));
		return new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK);
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
	public ResponseEntity<Map<String, Set<IcConnectorInfo>>> getAvailableLocalConnectors(
			@ApiParam(value = "Connector framework.", example = "connId", defaultValue = "connId")
			@RequestParam(required = false) String framework) {
		Map<String, Set<IcConnectorInfo>> infos = new HashMap<>();
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
		return new ResponseEntity<Map<String, Set<IcConnectorInfo>>>(infos, HttpStatus.OK);
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
	public ResponseEntity<Map<String, Set<IcConnectorInfo>>> getAvailableRemoteConnectors(
			@ApiParam(value = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		SysSystemDto dto = this.getDto(backendId);

		Map<String, Set<IcConnectorInfo>> infos = new HashMap<>();
		
		// if entity hasn't set up for remote return empty map
		if (dto == null || !dto.isRemote()) {
			return new ResponseEntity<Map<String, Set<IcConnectorInfo>>>(infos, HttpStatus.OK);
		}

 		Assert.notNull(dto.getConnectorServer(), "Connector server is required.");
 		//
 		try {
 			for (IcConfigurationService config: icConfiguration.getIcConfigs().values()) {
				SysConnectorServerDto server = dto.getConnectorServer();
				server.setPassword(this.confidentialStorage.getGuardedString(dto.getId(), SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD));
				infos.put(config.getFramework(), config.getAvailableRemoteConnectors(server));
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
	 * Get available bulk actions for role
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@ApiOperation(
			value = "Get available bulk actions", 
			nickname = "availableBulkAction", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "") })
				})
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	/**
	 * Process bulk action for roles
	 *
	 * @param bulkAction
	 * @return
	 */
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@ApiOperation(
			value = "Process bulk action for role", 
			nickname = "bulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")})
				})
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	/**
	 * Prevalidate bulk action for roles
	 *
	 * @param bulkAction
	 * @return
	 */
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@ApiOperation(
			value = "Prevalidate bulk action for role", 
			nickname = "prevalidateBulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")})
				})
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	/**
	 * Long polling for check sync in progress for given system
	 *  
	 * @param backendId - system ID
	 * 
	 * @return DeferredResult<OperationResultDto>, where:
	 * 
	 * - RUNNING = Some sync are not resolved, but some sync was changed (since previous check).
	 * - NOT_EXECUTED = Deferred-result expired
	 * - BLOCKED - Long polling is disabled
	 * 
	 */
	@ResponseBody
	@RequestMapping(value = "{backendId}/check-running-sync", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@ApiOperation(
			value = "Check changes of unresloved sync for the system (Long-polling request).", 
			nickname = "checkRunningSyncs", 
			tags = { SysSystemController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")})
				})
	public DeferredResult<OperationResultDto> checkRunningSyncs(
			@ApiParam(value = "System's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		SysSystemDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		UUID systemId = dto.getId();

		DeferredResultWrapper result = new DeferredResultWrapper( //
				systemId, //
				dto.getClass(),//
				new DeferredResult<OperationResultDto>( //
						30000l, new OperationResultDto(OperationState.NOT_EXECUTED)) //
		); //

		result.onCheckResultCallback(new CheckLongPollingResult() {

			@Override
			public void checkDeferredResult(DeferredResult<OperationResultDto> result,
					LongPollingSubscriber subscriber) {
				checkDeferredRequest(result, subscriber);
			}
		});

		// If isn't long polling enabled, then Blocked response will be sent.
		if (!longPollingManager.isLongPollingEnabled()) {
			result.getResult().setResult(new OperationResultDto(OperationState.BLOCKED));
			return result.getResult();
		}

		longPollingManager.addSuspendedResult(result);

		return result.getResult();
	}
	
	@Scheduled(fixedDelay = 2000)
	public synchronized void checkDeferredRequests() {
		longPollingManager.checkDeferredRequests(SysSystemDto.class);
	}

	/**
	 * Check deferred result - using default implementation from long-polling-manager.
	 * 
	 * @param deferredResult
	 * @param subscriber
	 */
	public void checkDeferredRequest(DeferredResult<OperationResultDto> deferredResult, LongPollingSubscriber subscriber) {
		Assert.notNull(deferredResult, "Deferred result is required.");
		Assert.notNull(subscriber.getEntityId(), "Entity identifier is required.");
		
		SysSyncLogFilter filterLog = new SysSyncLogFilter();
		filterLog.setSystemId(subscriber.getEntityId());
		longPollingManager.baseCheckDeferredResult(deferredResult, subscriber, filterLog, syncLogService, false);
		
		if(deferredResult.isSetOrExpired()) {
			return;
		}
		SysSyncItemLogFilter filter = new SysSyncItemLogFilter();
		filter.setSystemId(subscriber.getEntityId());
		longPollingManager.baseCheckDeferredResult(deferredResult, subscriber, filter, syncItemLogService, true);
	}
	
	/**
	 * Returns definition for given system 
	 * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
	 * 
	 * @param system
	 * @return
	 */
	private synchronized IdmFormDefinitionDto getConnectorFormDefinition(SysSystemDto system) {
		Assert.notNull(system, "System is required.");
		//
		// connector key can't be null
		if (system.getConnectorKey() == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_FORM_DEFINITION_NOT_FOUND, ImmutableMap.of("system", system.getId()));
		}
		// for remote connector form definition we need password for remote connector server
		if (system.isRemote()) {
			SysConnectorServerDto connectorServer = system.getConnectorServer();
			connectorServer.setPassword(this.confidentialStorage.getGuardedString(system.getId(), SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD));
			system.setConnectorServer(connectorServer);
		}
		//
		return systemService.getConnectorFormDefinition(system.getConnectorInstance());
	}
	
	private synchronized IdmFormDefinitionDto getPoolingConnectorFormDefinition(SysSystemDto system) {
		Assert.notNull(system, "System is required.");
		//
		// connector key can't be null
		if (system.getConnectorKey() == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_FORM_DEFINITION_NOT_FOUND, ImmutableMap.of("system", system.getId()));
		}
		//
		return systemService.getPoolingConnectorFormDefinition(system.getConnectorInstance());
	}
	
	@Override
	protected SysSystemFilter toFilter(MultiValueMap<String, Object> parameters) {
		SysSystemFilter filter = new SysSystemFilter(parameters);
		//
		filter.setPasswordPolicyValidationId(getParameterConverter().toUuid(parameters, "passwordPolicyValidationId"));
		filter.setPasswordPolicyGenerationId(getParameterConverter().toUuid(parameters, "passwordPolicyGenerationId"));
		filter.setVirtual(getParameterConverter().toBoolean(parameters, "virtual"));
		return filter;
	}
}
