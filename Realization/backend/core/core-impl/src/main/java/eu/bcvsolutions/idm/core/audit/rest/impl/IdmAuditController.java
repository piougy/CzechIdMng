package eu.bcvsolutions.idm.core.audit.rest.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.audit.dto.IdmAuditDiffDto;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmAuditEntityDto;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmAuditFilter;
import eu.bcvsolutions.idm.core.api.audit.service.IdmAuditService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.audit.entity.IdmAudit_;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * IdM audit endpoint.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/audits")
@Api(
		value = IdmAuditController.TAG, 
		description = "Read / search audit log", 
		tags = { IdmAuditController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmAuditController extends AbstractReadWriteDtoController<IdmAuditDto, IdmAuditFilter> {

	protected static final String TAG = "Audit";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmAuditController.class);
	//
	private final IdmAuditService auditService;
	//
	@Autowired private ModelMapper mapper;
	
	@Autowired
	public IdmAuditController(IdmAuditService auditService) {
		super(auditService);
		//
		this.auditService = auditService;
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search audit logs", 
			nickname = "searchQuickAudits", 
			tags = { IdmAuditController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return this.find(parameters, pageable);
	}
	
	@Override
	public Page<IdmAuditDto> find(IdmAuditFilter filter, Pageable pageable, BasePermission permission) {
		Page<IdmAuditDto> dtos = super.find(filter, pageable, permission);
		Map<UUID, BaseDto> loadedDtos = new HashMap<>();
		dtos.forEach(dto -> {
			loadEmbeddedEntity(loadedDtos, dto);
		});
		//
		return dtos;
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@RequestMapping(value= "/search/entity", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search audit logs related to entity", 
			nickname = "searchEntity", 
			tags = { IdmAuditController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") })
				})
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entity", allowMultiple = false, dataType = "string", paramType = "query",
				value = "Entity class - find related audit log to this class"),
        @ApiImplicitParam(name = "page", dataType = "string", paramType = "query",
                value = "Results page you want to retrieve (0..N)"),
        @ApiImplicitParam(name = "size", dataType = "string", paramType = "query",
                value = "Number of records per page."),
        @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                value = "Sorting criteria in the format: property(,asc|desc). " +
                        "Default sort order is ascending. " +
                        "Multiple sort criteria are supported.")
	})
	public Resources<?> findEntity(
			@RequestParam(required = false) String entityClass,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		//
		// Because backward compatibility there must be set entity class and other useless parameters
		IdmAuditFilter filter = toFilter(parameters);
		if (StringUtils.isEmpty(filter.getOwnerType())) {
			throw new ResultCodeException(CoreResultCode.AUDIT_ENTITY_CLASS_IS_NOT_FILLED);
		}

		// Backward compatibility
		if (StringUtils.isEmpty(filter.getOwnerCode()) && parameters.containsKey(IdmIdentity_.username.getName())) {
			Object identityUsername = parameters.getFirst(IdmIdentity_.username.getName());
			if (identityUsername != null) {
				filter.setOwnerCode(identityUsername.toString());
			}
		}
		//
		Page<IdmAuditDto> dtos = auditService.find(filter, pageable);
		Map<UUID, BaseDto> loadedDtos = new HashMap<>();
		dtos.forEach(dto -> {
			loadEmbeddedEntity(loadedDtos, dto);
		});
		return toResources(dtos, getDtoClass());

	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@RequestMapping(value= "/search/login", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search audit logs for login identities", 
			nickname = "searchLoginAudits", 
			tags = { IdmAuditController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") })
				})
	public Resources<?> findLogin(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		// Password hasn't own rest controller -> audit is solved by audit controller.
		return this.toResources(this.auditService.findLogin(toFilter(parameters), pageable), getDtoClass());
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@RequestMapping(value= "/search/entities", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search audited entity classes", 
			nickname = "findAllAuditedEntities", 
			tags = { IdmAuditController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") })
				},
			notes = "Method return list of class simple name for which is audited."
					+ " Must at least one attribute withannotation {@value Audited}")
	public ResponseEntity<?> findAuditedEntity() {
		List<String> entities = auditService.getAllAuditedEntitiesNames();
		return new ResponseEntity<>(toResources(entities, null), HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@Override
	@ApiOperation(
			value = "Audit log detail", 
			nickname = "getAuditLog", 
			response = IdmAuditDto.class, 
			tags = { IdmAuditController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Audit log's identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmAuditDto audit = auditService.get(backendId);
		
		// Map with all values
		Map<String, Object> revisionValues = null;
		
		Object revision = null;
		try {
			revision = auditService.findVersion(Class.forName(audit.getType()), audit.getEntityId(), Long.valueOf(audit.getId().toString()));
		} catch (NumberFormatException | ClassNotFoundException e) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of("audit", audit), e);
		}
		
		revisionValues = auditService.getValuesFromVersion(revision);
		
		// create DTO and fill with values from IdmAudit
		IdmAuditDto auditDto = new IdmAuditDto();
		mapper.map(audit, auditDto);
		auditDto.setRevisionValues(revisionValues);
		
		return new ResponseEntity<IdmAuditDto>(auditDto, HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{revId}/diff/previous")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@ApiOperation(
			value = "Audit log detail", 
			nickname = "getAuditLogPreviousVersion", 
			response = IdmAuditDto.class, 
			tags = { IdmAuditController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") })
				},
			notes = "Returns previous version for given audit log")
	public ResponseEntity<?> previousVersion(
			@ApiParam(value = "Audit log's identifier.", required = true)
			@PathVariable @NotNull String revId) {
		IdmAuditDto currentAudit = auditService.get(revId);
		IdmAuditDto previousAudit;
		ResponseEntity<IdmAuditDto> resource = null;
		
		try {
			IdmAuditDto dto = null;
			previousAudit = auditService.findPreviousRevision(currentAudit.getId());
			//
			// previous version dost'n exist
			if (previousAudit != null) {
				dto = new IdmAuditDto();
				mapper.map(previousAudit, dto);
				dto.setRevisionValues(
						auditService.getValuesFromVersion(
								auditService.findPreviousVersion(
										Class.forName(previousAudit.getType()),
										previousAudit.getEntityId(),
										previousAudit.getId())));
				resource = new ResponseEntity<IdmAuditDto>(dto, HttpStatus.OK);
			} else {
				resource = new ResponseEntity<IdmAuditDto>(HttpStatus.NOT_FOUND);
			}
			
		} catch (ClassNotFoundException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("audit class", currentAudit.getType()), e);
		}
		
		return resource;
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{firstRevId}/diff/{secondRevId}")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@ApiOperation(
			value = "Audit log diff", 
			nickname = "getAuditLogDiff", 
			response = IdmAuditDiffDto.class, 
			tags = { IdmAuditController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") })
				},
			notes = "Returns diff between given audit logs versions")
	public ResponseEntity<?> diff(
			@ApiParam(value = "Audit log's identifier.", required = true)
			@PathVariable @NotNull String firstRevId, 
			@ApiParam(value = "Audit log's identifier.", required = true)
			@PathVariable String secondRevId) {
		IdmAuditDiffDto dto = new IdmAuditDiffDto();
		dto.setDiffValues(auditService.getDiffBetweenVersion(Long.valueOf(firstRevId), Long.valueOf(secondRevId)));
		dto.setIdFirstRevision(Long.valueOf(firstRevId));
		dto.setIdSecondRevision(Long.valueOf(secondRevId));
		
		return new ResponseEntity<IdmAuditDiffDto>(dto, HttpStatus.OK);
	}
	
	/**
	 * Fills referenced entity to dto - prevent to load entity for each row.
	 * 
	 * @param dto
	 */
	private void loadEmbeddedEntity(Map<UUID, BaseDto> loadedDtos, IdmAuditDto dto) {
		UUID entityId = dto.getEntityId();
		if (entityId == null || StringUtils.isEmpty(dto.getType())) {
			return; // just for sure - IdmAudit entity doesn't specify it as required (but it should be)
		}
		//
		BaseDto revision = null;
		if (loadedDtos.containsKey(entityId)) {
			revision = loadedDtos.get(entityId);
		} else {
			try {
				revision = getLookupService().lookupDto(dto.getType(), entityId);
				loadedDtos.put(entityId, revision);
			} catch (IllegalArgumentException ex) {
				LOG.debug("Class [{}] not found on classpath (e.g. module was uninstalled)", dto.getType(), ex);
			}
		}
		dto.getEmbedded().put(IdmAudit_.entityId.getName(), revision); // nullable
		//
		// try to load last revision for deleted entity - main table only ~ subowner will not be soled
		if (revision == null) {
			try {
				Object lastPersistedVersion = auditService.findLastPersistedVersion(Class.forName(dto.getType()), entityId);
				if (lastPersistedVersion != null) {
					// FIXME: this returns entity => i need to map it to dto properly => #978
					Map<String, Object> valuesFromVersion = auditService.getValuesFromVersion(lastPersistedVersion);
					if (!valuesFromVersion.containsKey(Auditable.PROPERTY_ID)) {
						// id is not in values by default
						valuesFromVersion.put(Auditable.PROPERTY_ID, entityId);
					}
					dto.setRevisionValues(valuesFromVersion);
				}
			} catch (IllegalArgumentException | ClassNotFoundException ex) {
				LOG.debug("Class [{}] not found on classpath (e.g. module was uninstalled)", dto.getType(), ex);
			}
		}
		//
		// For subowner, some entities doesn't support owner and subowner.
		if (dto.getSubOwnerId() != null) {
			try {
				UUID subOwnerId = UUID.fromString(dto.getSubOwnerId());
				if (!loadedDtos.containsKey(subOwnerId)) {
					loadedDtos.put(subOwnerId, getLookupService().lookupDto(dto.getSubOwnerType(), subOwnerId));
				}
				dto.getEmbedded().put(IdmAudit_.subOwnerId.getName(), loadedDtos.get(subOwnerId));
			} catch (IllegalArgumentException ex) {
				LOG.debug("Class [{}] not found on classpath (e.g. module was uninstalled)", dto.getSubOwnerType(), ex);
			}
		}
		// For owner, some entities doesn't support owner and subowner.
		if (dto.getOwnerId() != null) {
			try {
				UUID ownerId = UUID.fromString(dto.getOwnerId());
				if (!loadedDtos.containsKey(ownerId)) {
					loadedDtos.put(ownerId, getLookupService().lookupDto(dto.getOwnerType(), ownerId));
				}
				dto.getEmbedded().put(IdmAudit_.ownerId.getName(), loadedDtos.get(ownerId));
			} catch (IllegalArgumentException ex) {
				LOG.debug("Class [{}] not found on classpath (e.g. module was uninstalled)", dto.getSubOwnerType(), ex);
			}
		}
		// Fill embedded contract for FE agenda (prevent to load contract for each row).
		if ((dto instanceof IdmAuditEntityDto) && dto.getType().equals(IdmIdentityRole.class.getCanonicalName())) {
			IdmAuditEntityDto auditEntity = (IdmAuditEntityDto) dto;
			if (auditEntity.getEntity().containsKey(IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT)
					&& !auditEntity.getEmbedded().containsKey(IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT)) {
				UUID contractId = DtoUtils.toUuid(auditEntity.getEntity().get(IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT));
				if (contractId != null) {
					if (!loadedDtos.containsKey(contractId)) {
						loadedDtos.put(contractId, getLookupService().lookupDto(IdmIdentityContractDto.class, contractId));
					}
					auditEntity.getEmbedded().put(IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT, loadedDtos.get(contractId));
				}				
			}
		}
	}

	@Override
	protected IdmAuditFilter toFilter(MultiValueMap<String, Object> parameters) {
		// We must check if map contains list of changed attributes, because mapped doesn't works with list and zero values.
		List<String> changedAttributesList = null;
		if (parameters.containsKey("changedAttributesList")) {
			List<Object> remove = parameters.remove("changedAttributesList");
			changedAttributesList = remove.stream().map(o -> Objects.toString(o.toString())).collect(Collectors.toList());
		}
		// entity id decorator
		String entityId = getParameterConverter().toString(parameters, "entityId");
		String entityType = getParameterConverter().toString(parameters, "type");
		UUID entityUuid = null;
		if (StringUtils.isNotEmpty(entityType) && StringUtils.isNotEmpty(entityId)) {
			// try to find entity by Codeable identifier
			AbstractDto entity = getLookupService().lookupDto(entityType, entityId);
			if (entity != null) {
				entityUuid = entity.getId();
				parameters.remove("entityId");
			} else {
				LOG.debug("Entity type [{}] with identifier [{}] does not found, raw entityId will be used as uuid.", 
						entityType, entityId);
				// Better exception for FE.
				try {
					DtoUtils.toUuid(entityId);
				} catch (ClassCastException ex) {
					throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", entityId), ex);
				}
			}
		}
		IdmAuditFilter filter = super.toFilter(parameters);
		filter.setChangedAttributesList(changedAttributesList);
		if (entityUuid != null) {
			filter.setEntityId(entityUuid);
		}
		//
		return filter;
	}
}
