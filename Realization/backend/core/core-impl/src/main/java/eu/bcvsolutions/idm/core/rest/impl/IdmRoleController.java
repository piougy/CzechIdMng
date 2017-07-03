package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteEntityController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmRoleFormValue;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuditService;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

/**
 * Endpoint for roles
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/roles")
@Api(value = "Roles", tags = "Roles", description = "Operations with roles")
public class IdmRoleController extends AbstractReadWriteEntityController<IdmRole, RoleFilter> {
	
	private final IdmAuditService auditService;
	private final IdmFormDefinitionController formDefinitionController;
	private final IdmAuthorizationPolicyService authorizationPolicyService;
	private final SecurityService securityService;
	private final FormService formService;
	
	@Autowired
	public IdmRoleController(
			LookupService entityLookupService, 
			IdmAuditService auditService,
			IdmAuthorizationPolicyService authorizationPolicyService,
			IdmFormDefinitionController formDefinitionController,
			SecurityService securityService,
			FormService formService) {
		super(entityLookupService);
		//
		Assert.notNull(auditService);
		Assert.notNull(formDefinitionController);
		Assert.notNull(authorizationPolicyService);
		Assert.notNull(securityService);
		Assert.notNull(formService);
		//
		this.auditService = auditService;
		this.formDefinitionController = formDefinitionController;
		this.authorizationPolicyService = authorizationPolicyService;
		this.securityService = securityService;
		this.formService = formService;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.findQuick(parameters, pageable, assembler);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_AUTOCOMPLETE + "')")
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.autocomplete(parameters, pageable, assembler);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@ApiOperation(value = "Role detail", nickname = "getRole", response = IdmRole.class, tags={ "Roles" }, authorizations = { 
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		return super.get(backendId, assembler);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_CREATE + "') or hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	public ResponseEntity<?> post(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.post(nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	public ResponseEntity<?> put(
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.put(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) 
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	public Set<String> getPermissions(@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@ResponseBody
	@RequestMapping(value = "{roleId}/revisions/{revId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	public ResponseEntity<?> findRevision(@PathVariable("roleId") String roleId, @PathVariable("revId") Long revId, PersistentEntityResourceAssembler assembler) {
		IdmRole originalEntity = getEntity(roleId);
		if (originalEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("role", roleId));
		}
		checkAccess(originalEntity, IdmBasePermission.READ);
		//
		IdmRole revisionRole;
		try {
			revisionRole = this.auditService.findRevision(IdmRole.class, originalEntity.getId(), revId);
			checkAccess(revisionRole, IdmBasePermission.READ);
		} catch (RevisionDoesNotExistException ex) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", roleId), ex);
		}
		
		return new ResponseEntity<>(toResource(revisionRole, assembler), HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "{roleId}/revisions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	public Resources<?> findRevisions(@PathVariable("roleId") String roleId, Pageable pageable, 
			PersistentEntityResourceAssembler assembler) {
		IdmRole originalEntity = getEntity(roleId);
		if (originalEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("role", roleId));
		}
		checkAccess(originalEntity, IdmBasePermission.READ);
		//
		Page<IdmAuditDto> results = this.auditService.findRevisionsForEntity(IdmRole.class.getSimpleName(), UUID.fromString(roleId), pageable);
		return toResources(results, assembler, IdmRole.class, null);
	}
	
	/**
	 * Returns form definition to given entity.
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	public ResponseEntity<?> getFormDefinitions(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		return formDefinitionController.getDefinitions(IdmRole.class, assembler);
	}
	
	/**
	 * Returns entity's filled form values
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	public Resources<?> getFormValues(
			@PathVariable @NotNull String backendId, 
			@RequestParam(name = "definitionCode") String definitionCode,
			PersistentEntityResourceAssembler assembler) {
		IdmRole entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(entity, IdmBasePermission.READ);
		//
		IdmFormDefinition formDefinition = null; // default
		if (StringUtils.isNotEmpty(definitionCode)) {
			formDefinition = formService.getDefinition(IdmRole.class, definitionCode);
		}
		//
		return formDefinitionController.getFormValues(entity, formDefinition, assembler);
	}
	
	/**
	 * Saves entity's form values
	 * 
	 * @param backendId
	 * @param formValues
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.POST)
	public Resources<?> saveFormValues(
			@PathVariable @NotNull String backendId,
			@RequestParam(name = "definitionCode") String definitionCode,
			@RequestBody @Valid List<IdmRoleFormValue> formValues,
			PersistentEntityResourceAssembler assembler) {		
		IdmRole entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(entity, IdmBasePermission.UPDATE);
		//
		IdmFormDefinition formDefinition = null; // default
		if (StringUtils.isNotEmpty(definitionCode)) {
			formDefinition = formService.getDefinition(IdmRole.class, definitionCode);
		}
		//
		return formDefinitionController.saveFormValues(entity, formDefinition, formValues, assembler);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@RequestMapping(value = "/{backendId}/authorities", method = RequestMethod.GET)
	public Set<GrantedAuthority> getAuthorities(@PathVariable @NotNull String backendId) {
		IdmRole entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(entity, IdmBasePermission.READ);
		//
		return authorizationPolicyService.getEnabledRoleAuthorities(securityService.getAuthentication().getCurrentIdentity().getId(), entity.getId());
	}
	
	@Override
	protected IdmRole validateEntity(IdmRole entity) {
		// TODO: remove after dto refactoring or remove guarantees from role at all (create standalone endpoint).
		entity.getGuarantees().forEach(guarantee -> {
			if (guarantee.getGuarantee() != null && guarantee.getGuarantee().getId() != null) {
				guarantee.setGuarantee((IdmIdentity) entityLookupService.lookupEntity(IdmIdentity.class, guarantee.getGuarantee().getId()));
			}
		});
		// TODO: remove after dto refactoring or remove catalogue from role at all (create standalone endpoint).
		entity.getRoleCatalogues().forEach(catalogue -> {
			if (catalogue.getRoleCatalogue() != null && catalogue.getRoleCatalogue().getId() != null) {
				catalogue.setRoleCatalogue((IdmRoleCatalogue) entityLookupService.lookupEntity(IdmRoleCatalogue.class, catalogue.getRoleCatalogue().getId()));
			}
		});
		//
		return super.validateEntity(entity);
	}
	
	@Override
	protected RoleFilter toFilter(MultiValueMap<String, Object> parameters) {
		RoleFilter filter = new RoleFilter();
		filter.setId(getParameterConverter().toUuid(parameters, "id"));
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setRoleType(getParameterConverter().toEnum(parameters, "roleType", RoleType.class));
		filter.setRoleCatalogue(getParameterConverter().toEntity(parameters, "roleCatalogue", IdmRoleCatalogue.class));
		filter.setGuarantee(getParameterConverter().toEntity(parameters, "guarantee", IdmIdentity.class));
		return filter;
	}
}
