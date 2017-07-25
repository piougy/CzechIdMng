package eu.bcvsolutions.idm.core.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.forest.index.service.api.ForestContentService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.audit.service.api.IdmAuditService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.WorkPositionDto;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.service.GrantedAuthoritiesFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Rest methods for IdmIdentity resource
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestController // TODO: @RestController after eav to dto - after PersistentEntityResourceAssembler will be removed from method parameter
@RequestMapping(value = BaseDtoController.BASE_PATH + "/identities") //produces= BaseController.APPLICATION_HAL_JSON_VALUE - I have to remove this (username cannot have "@.com" in user name)
@Api(
		value = IdmIdentityController.TAG,  
		tags = { IdmIdentityController.TAG }, 
		description = "Operations with identities",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmIdentityController extends AbstractReadWriteDtoController<IdmIdentityDto, IdentityFilter> {

	protected static final String TAG = "Identities";
	//
	private final GrantedAuthoritiesFactory grantedAuthoritiesFactory;
	private final IdmIdentityContractService identityContractService;
	private final IdmIdentityRoleService identityRoleService;
	private final IdmAuditService auditService; 	
	private final IdmTreeNodeService treeNodeService;
	private final ForestContentService<IdmTreeNode, IdmForestIndexEntity, UUID> treeNodeIndexService;
	private final IdmIdentityRepository identityRepository;
	//
	private final IdmFormDefinitionController formDefinitionController;
	
	@Autowired
	public IdmIdentityController(
			IdmIdentityService identityService, 
			IdmFormDefinitionController formDefinitionController,
			GrantedAuthoritiesFactory grantedAuthoritiesFactory,
			IdmIdentityContractService identityContractService,
			IdmIdentityRoleService identityRoleService,
			IdmAuditService auditService,
			ForestContentService<IdmTreeNode, IdmForestIndexEntity, UUID> treeNodeIndexService,
			IdmTreeNodeService treeNodeService,
			IdmIdentityRepository identityRepository) {
		super(identityService);
		//
		Assert.notNull(formDefinitionController);
		Assert.notNull(grantedAuthoritiesFactory);
		Assert.notNull(identityContractService);
		Assert.notNull(identityRoleService);
		Assert.notNull(auditService);
		Assert.notNull(treeNodeIndexService);
		Assert.notNull(treeNodeService);
		Assert.notNull(identityRepository);
		//
		this.formDefinitionController = formDefinitionController;
		this.grantedAuthoritiesFactory = grantedAuthoritiesFactory;
		this.identityContractService = identityContractService;
		this.identityRoleService = identityRoleService;
		this.auditService = auditService;
		this.treeNodeIndexService = treeNodeIndexService;
		this.treeNodeService = treeNodeService;
		this.identityRepository = identityRepository;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@ApiOperation(
			value = "Search identities (/search/quick alias)", 
			nickname = "searchIdentities", 
			tags = { IdmIdentityController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@ApiOperation(
			value = "Search identities", 
			nickname = "searchQuickIdentities", 
			tags = { IdmIdentityController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete identities (selectbox usage)", 
			nickname = "autocompleteIdentities", 
			tags = { IdmIdentityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@ApiOperation(
			value = "Identity detail", 
			nickname = "getIdentity", 
			response = IdmIdentityDto.class, 
			tags = { IdmIdentityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.IDENTITY_UPDATE + "')")
	@ApiOperation(
			value = "Create / update identity", 
			nickname = "postIdentity", 
			response = IdmIdentityDto.class, 
			tags = { IdmIdentityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody IdmIdentityDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_UPDATE + "')")
	@ApiOperation(
			value = "Update identity", 
			nickname = "putIdentity", 
			response = IdmIdentityDto.class, 
			tags = { IdmIdentityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmIdentityDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_DELETE + "')")
	@ApiOperation(
			value = "Delete identity", 
			nickname = "deleteIdentity", 
			tags = { IdmIdentityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnIdentity", 
			tags = { IdmIdentityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	/**
	 * Returns given identity's granted authorities
	 * 
	 * @param backendId
	 * @return list of granted authorities
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/authorities", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@ApiOperation(
			value = "Identity granted authorities", 
			nickname = "getIdentityAuthorities", 
			tags = { IdmIdentityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") })
				})
	public List<? extends GrantedAuthority> getGrantedAuthotrities(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable String backendId) {
		IdmIdentityDto identity = getDto(backendId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(identity, IdmBasePermission.READ);
		//
		return grantedAuthoritiesFactory.getGrantedAuthorities(identity.getUsername());
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/roles", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@ApiOperation(
			value = "Assigned roles to identity", 
			nickname = "getIdentityRoles", 
			tags = { IdmIdentityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") })
				})
	public Resources<?> roles(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable String backendId, 
			PersistentEntityResourceAssembler assembler) {	
		IdmIdentityDto identity = getDto(backendId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		checkAccess(identity, IdmBasePermission.READ);
		//
		IdentityRoleFilter filter = new IdentityRoleFilter();
		filter.setIdentityId(identity.getId());		
		Page<IdmIdentityRoleDto> identityRoles = identityRoleService.find(filter, null, IdmBasePermission.READ);
		//
		return toResources(identityRoles, IdmIdentityRoleDto.class);
	}
	
	/**
	 * Get given identity's prime position in organization.
	 * 
	 * @param backendId
	 * @param assembler
	 * @return Positions from root to closest parent
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/work-position", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@ApiOperation(
			value = "Identity prime position in organization.", 
			nickname = "getIdentityPosition", 
			tags = { IdmIdentityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") })
				})
	public ResponseEntity<?> organizationPosition(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable String backendId, 
			PersistentEntityResourceAssembler assembler) {
		IdmIdentityDto identity = getDto(backendId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		checkAccess(identity, IdmBasePermission.READ);
		//
		IdmIdentityContractDto primeContract = identityContractService.getPrimeContract(identity.getId());
		if (primeContract == null) {
			return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
		}
		WorkPositionDto position = new WorkPositionDto(identity, primeContract);
		if (primeContract.getWorkPosition() != null) {
			List<IdmTreeNode> positions = new ArrayList<>();
			// TODO: tree node service to dtos
			IdmTreeNode contractPosition = treeNodeService.get(primeContract.getWorkPosition());
			positions = treeNodeIndexService.findAllParents(contractPosition, new Sort(Direction.ASC, "forestIndex.lft"));
			positions.add(contractPosition);
			positions.forEach(treeNode -> {
				// TODO: use DTOs!
				treeNode.setTreeType(null);
				treeNode.setParent(null);
			});
			position.setPath(positions);
		}		
		return new ResponseEntity<WorkPositionDto>(position, HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/revisions/{revId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@ApiOperation(
			value = "Identity audit - read revision detail", 
			nickname = "getIdentityRevision", 
			tags = { IdmIdentityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") })
				})
	public ResponseEntity<?> findRevision(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable("backendId") String backendId, 
			@ApiParam(value = "Revision identifier.", required = true)
			@PathVariable("revId") Long revId) {
		IdmIdentityDto originalEntity = getDto(backendId);
		if (originalEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(originalEntity, IdmBasePermission.READ);
		//
		IdmIdentity revisionIdentity;
		try {
			revisionIdentity = this.auditService.findRevision(IdmIdentity.class, originalEntity.getId(), revId);
			// checkAccess(revisionIdentity, IdmBasePermission.READ);
		} catch (RevisionDoesNotExistException ex) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", revId), ex);
		}
		// TODO: dto
		return new ResponseEntity<>(revisionIdentity, HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/revisions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@ApiOperation(
			value = "Identity audit - read all revisions", 
			nickname = "getIdentityRevisions", 
			tags = { IdmIdentityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") })
				})
	public Resources<?> findRevisions(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable("backendId") String backendId, 
			Pageable pageable,
			PersistentEntityResourceAssembler assembler) {
		IdmIdentityDto originalEntity = getDto(backendId);
		if (originalEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("identity", backendId));
		}
		//
		checkAccess(originalEntity, IdmBasePermission.READ);
		// get original entity id
		Page<IdmAuditDto> results = this.auditService.findRevisionsForEntity(IdmIdentity.class.getSimpleName(), originalEntity.getId(), pageable);
		return toResources(results, IdmAuditDto.class);
	}
	
	/**
	 * Returns form definition to given identity.
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@ApiOperation(
			value = "Identity extended attributes form definitions", 
			nickname = "getIdentityFormDefinitions", 
			tags = { IdmIdentityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") })
				})
	public ResponseEntity<?> getFormDefinitions(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId, 
			PersistentEntityResourceAssembler assembler) {
		return formDefinitionController.getDefinitions(IdmIdentity.class, assembler);
	}
	
	/**
	 * Returns filled form values
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	@ApiOperation(
			value = "Identity form definition - read values", 
			nickname = "getIdentityFormValues", 
			tags = { IdmIdentityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") })
				})
	public Resources<?> getFormValues(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId, 
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode,
			PersistentEntityResourceAssembler assembler) {
		IdmIdentityDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		checkAccess(entity, IdmBasePermission.READ);
		//
		IdmFormDefinition formDefinition = formDefinitionController.getDefinition(IdmIdentity.class, definitionCode);
		//
		return formDefinitionController.getFormValues(identityRepository.findOne(entity.getId()), formDefinition, assembler);
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.POST)
	@ApiOperation(
			value = "Identity form definition - save values", 
			nickname = "postIdentityFormValues", 
			tags = { IdmIdentityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_UPDATE, description = "") })
				})
	public Resources<?> saveFormValues(
			@ApiParam(value = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId,
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode,
			@ApiParam(value = "Filled form data.", required = true)
			@RequestBody @Valid List<IdmIdentityFormValue> formValues,
			PersistentEntityResourceAssembler assembler) {		
		IdmIdentityDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(entity, IdmBasePermission.UPDATE);
		//
		IdmFormDefinition formDefinition = formDefinitionController.getDefinition(IdmIdentity.class, definitionCode);
		//
		return formDefinitionController.saveFormValues(identityRepository.findOne(entity.getId()), formDefinition, formValues, assembler);
	}
	
	@Override
	protected IdentityFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdentityFilter filter = new IdentityFilter(parameters);
		filter.setDisabled(getParameterConverter().toBoolean(parameters, "disabled"));
		filter.setSubordinatesFor(getParameterConverter().toEntityUuid(parameters, IdentityFilter.PARAMETER_SUBORDINATES_FOR, IdmIdentity.class));
		filter.setSubordinatesByTreeType(getParameterConverter().toEntityUuid(parameters, IdentityFilter.PARAMETER_SUBORDINATES_BY_TREE_TYPE, IdmTreeType.class));
		filter.setManagersFor(getParameterConverter().toEntityUuid(parameters, IdentityFilter.PARAMETER_MANAGERS_FOR, IdmIdentity.class));
		filter.setManagersByTreeType(getParameterConverter().toEntityUuid(parameters, IdentityFilter.PARAMETER_MANAGERS_BY_TREE_TYPE, IdmTreeType.class));
		filter.setTreeNode(getParameterConverter().toEntityUuid(parameters, "treeNodeId", IdmTreeNode.class));
		filter.setRecursively(getParameterConverter().toBoolean(parameters, "recursively", true));
		filter.setTreeType(getParameterConverter().toUuid(parameters, "treeTypeId"));
		filter.setManagersByContract(getParameterConverter().toUuid(parameters, IdentityFilter.PARAMETER_MANAGERS_BY_CONTRACT));
		filter.setIncludeGuarantees(getParameterConverter().toBoolean(parameters, "includeGuarantees", false));
		// TODO: or / and in multivalues? OR is supported now
		if (parameters.containsKey("role")) {
			for(Object role : parameters.get("role")) {
				filter.getRoles().add(getParameterConverter().toEntityUuid((String) role, IdmRole.class));
			}
		}
		filter.setFirstName(getParameterConverter().toString(parameters, "firstName"));
		filter.setLastName(getParameterConverter().toString(parameters, "lastName"));
		return filter;
	}
}
