package eu.bcvsolutions.idm.core.rest.impl;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.domain.IdmRoleType;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuditService;

/**
 * IdmRole endpoint
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/roles")
public class IdmRoleController extends DefaultReadWriteEntityController<IdmRole, RoleFilter> {
	
	private final IdmAuditService auditService; 
	
	@Autowired
	public IdmRoleController(
			EntityLookupService entityLookupService, 
			IdmAuditService auditService) {
		super(entityLookupService);
		//
		Assert.notNull(auditService);
		//
		this.auditService = auditService;
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_WRITE + "')")
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.create(nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_WRITE + "')")
	public ResponseEntity<?> update(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.update(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_WRITE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.ROLE_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@ResponseBody
	@RequestMapping(value = "{roleId}/revisions/{revId}", method = RequestMethod.GET)
	public ResponseEntity<?> findRevision(@PathVariable("roleId") String roleId, @PathVariable("revId") Long revId, PersistentEntityResourceAssembler assembler) {
		IdmRole originalEntity = getEntity(roleId);
		if (originalEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("role", roleId));
		}
		
		IdmRole revisionRole;
		try {
			revisionRole = this.auditService.findRevision(IdmRole.class, originalEntity.getId(), revId);
		} catch (RevisionDoesNotExistException ex) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", roleId), ex);
		}
		
		return new ResponseEntity<>(toResource(revisionRole, assembler), HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "{roleId}/revisions", method = RequestMethod.GET)
	public Resources<?> findRevisions(@PathVariable("roleId") String roleId, Pageable pageable, 
			PersistentEntityResourceAssembler assembler) {
		IdmRole originalEntity = getEntity(roleId);
		if (originalEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("role", roleId));
		}
		
		Page<IdmAudit> results = this.auditService.getRevisionsForEntity(IdmRole.class.getSimpleName(), UUID.fromString(roleId), pageable);
		
		return toResources(results, assembler, IdmRole.class, null);
	}
	
	@Override
	protected RoleFilter toFilter(MultiValueMap<String, Object> parameters) {
		RoleFilter filter = new RoleFilter();
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setRoleType(getParameterConverter().toEnum(parameters, "roleType", IdmRoleType.class));
		filter.setRoleCatalogue(getParameterConverter().toEntity(parameters, "roleCatalogue", IdmRoleCatalogue.class));
		filter.setGuarantee(getParameterConverter().toEntity(parameters, "guarantee", IdmIdentity.class));
		return filter;
	}
}
