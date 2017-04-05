package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteEntityController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.domain.RoleType;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmRoleFormValue;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuditService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Endpoint for roles
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/roles")
public class IdmRoleController extends AbstractReadWriteEntityController<IdmRole, RoleFilter> {
	
	private final IdmAuditService auditService;
	private final IdmFormDefinitionController formDefinitionController;
	
	@Autowired
	public IdmRoleController(
			EntityLookupService entityLookupService, 
			IdmAuditService auditService,
			IdmFormDefinitionController formDefinitionController) {
		super(entityLookupService);
		//
		Assert.notNull(auditService);
		Assert.notNull(formDefinitionController);
		//
		this.auditService = auditService;
		this.formDefinitionController = formDefinitionController;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	public Resources<?> find(@RequestParam MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_AUTOCOMPLETE + "')")
	public Resources<?> autocomplete(@RequestParam MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.autocomplete(parameters, pageable, assembler);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
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
		Page<IdmAudit> results = this.auditService.getRevisionsForEntity(IdmRole.class.getSimpleName(), UUID.fromString(roleId), pageable);
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
	@RequestMapping(value = "/{backendId}/form-definition", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	public ResponseEntity<?> getFormDefinition(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		return formDefinitionController.getDefinition(IdmRole.class, assembler);
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
	public Resources<?> getFormValues(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		IdmRole entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(entity, IdmBasePermission.READ);
		//
		return formDefinitionController.getFormValues(entity, null, assembler);
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
			@RequestBody @Valid List<IdmRoleFormValue> formValues,
			PersistentEntityResourceAssembler assembler) {		
		IdmRole entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(entity, IdmBasePermission.UPDATE);
		//
		return formDefinitionController.saveFormValues(entity, null, formValues, assembler);
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
