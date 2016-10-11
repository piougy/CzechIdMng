package eu.bcvsolutions.idm.core.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.activiti.engine.runtime.ProcessInstance;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.data.rest.core.support.EntityLookup;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.QuickFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.repository.processor.RevisionAssembler;
import eu.bcvsolutions.idm.core.model.service.IdmAuditService;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.rest.lookup.IdmIdentityLookup;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.rest.WorkflowTaskInstanceController;
import eu.bcvsolutions.idm.security.service.GrantedAuthoritiesFactory;

/**
 * Rest methods for IdmIdentity resource
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/identities")
public class IdmIdentityController extends DefaultReadWriteEntityController<IdmIdentity, QuickFilter> {
	
	@Autowired
	private IdmIdentityLookup identityLookup;

	@Autowired
	private GrantedAuthoritiesFactory grantedAuthoritiesFactory;

	private IdmIdentityService identityService;
	
	@Autowired
	private IdmIdentityContractService identityContractService;

	@Autowired
	private WorkflowTaskInstanceController workflowTaskInstanceController;
	
	@Autowired
	private IdmAuditService auditService; 
	
	@Autowired
	public IdmIdentityController(IdmIdentityService identityService) {
		super(identityService);
		this.identityService = identityService;
	}

	@Override
	protected EntityLookup<IdmIdentity> getEntityLookup() {
		return identityLookup;
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.IDENTITY_WRITE + "')")
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.create(nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.IDENTITY_WRITE + "')")
	public ResponseEntity<?> update(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.update(backendId, nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.IDENTITY_WRITE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	/**
	 * Delete identity is not supported now
	 */
	@Override
	public void deleteEntity(IdmIdentity identity) {
		throw new ResultCodeException(CoreResultCode.METHOD_NOT_ALLOWED);
	}

	/**
	 * Returns given identity's granted authorities
	 * 
	 * @param identityId
	 * @return list of granted authorities
	 */
	@RequestMapping(value = "/{identityId}/authorities", method = RequestMethod.GET)
	public List<? extends GrantedAuthority> getGrantedAuthotrities(@PathVariable String identityId) {
		return grantedAuthoritiesFactory.getGrantedAuthorities(identityId);
	}

	/**
	 * Change given identity's permissions (assigned roles)
	 * @param identityId
	 * @return Instance of workflow user task, where applicant can fill his change permission request
	 */
	@RequestMapping(value = "/{identityId}/change-permissions", method = RequestMethod.PUT)
	public ResponseEntity<ResourceWrapper<WorkflowTaskInstanceDto>> changePermissions(@PathVariable String identityId) {	
		IdmIdentity identity = (IdmIdentity) identityLookup.lookupEntity(identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("identity", identityId));
		}
		ProcessInstance processInstance = identityService.changePermissions(identity);
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessInstanceId(processInstance.getId());
		List<ResourceWrapper<WorkflowTaskInstanceDto>> tasks = (List<ResourceWrapper<WorkflowTaskInstanceDto>>) workflowTaskInstanceController
				.search(filter).getBody().getResources();
		return new ResponseEntity<ResourceWrapper<WorkflowTaskInstanceDto>>(tasks.get(0), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{identityId}/roles", method = RequestMethod.GET)
	public Resources<?> roles(@PathVariable String identityId, PersistentEntityResourceAssembler assembler) {	
		IdmIdentity identity = (IdmIdentity) identityLookup.lookupEntity(identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("identity", identityId));
		}
		// TODO: IdmIdentityRoleService and pagination support?
		return toResources((Iterable<?>) identity.getRoles(), assembler, IdmIdentityRole.class, null);
	}
	
	@RequestMapping(value = "/{identityId}/identityContracts", method = RequestMethod.GET)
	public Resources<?> workingPositions(@PathVariable String identityId, PersistentEntityResourceAssembler assembler) {	
		IdmIdentity identity = (IdmIdentity) identityLookup.lookupEntity(identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("identity", identityId));
		}		
		return toResources((Iterable<?>) identityContractService.getContracts(identity), assembler, IdmIdentityContract.class, null);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{identityId}/revisions/{revId}", method = RequestMethod.GET)
	public ResponseEntity<ResourceWrapper<DefaultRevisionEntity>> findRevision(@PathVariable("identityId") String identityId, @PathVariable("revId") Integer revId) {
		IdmIdentity originalEntity = (IdmIdentity)this.identityLookup.lookupEntity(identityId);
		if (originalEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("identity", identityId));
		}
		
		Revision<Integer, ? extends AbstractEntity> revision;
		try {
			revision = this.auditService.findRevision(IdmIdentity.class, revId, originalEntity.getId());
		} catch (RevisionDoesNotExistException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", revId));
		}
		
		IdmIdentity entity = (IdmIdentity) revision.getEntity();
		RevisionAssembler<IdmIdentity> assembler = new RevisionAssembler<IdmIdentity>();
		ResourceWrapper<DefaultRevisionEntity> resource = assembler.toResource(this.getClass(),
				String.valueOf(this.identityLookup.getResourceIdentifier(entity)), revision, revId);

		return new ResponseEntity<ResourceWrapper<DefaultRevisionEntity>>(resource, HttpStatus.OK);
	}

	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{identityId}/revisions", method = RequestMethod.GET)
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>> findRevisions(@PathVariable("identityId") String identityId) {
		IdmIdentity originalEntity = (IdmIdentity)this.identityLookup.lookupEntity(identityId);
		if (originalEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("identity", identityId));
		}
		
		List<ResourceWrapper<DefaultRevisionEntity>> wrappers = new ArrayList<>();
		List<Revision<Integer, ? extends AbstractEntity>> revisions;
		try {
			revisions = this.auditService.findRevisions(IdmIdentity.class, originalEntity.getId());
		} catch (RevisionDoesNotExistException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", originalEntity.getId()));
		}
		
		RevisionAssembler<IdmIdentity> assembler = new RevisionAssembler<IdmIdentity>();
		
		for	(Revision<Integer, ? extends AbstractEntity> revision : revisions) {
			wrappers.add(assembler.toResource(this.getClass(), 
					String.valueOf(this.identityLookup.getResourceIdentifier((IdmIdentity)revision.getEntity())),
					revision, revision.getRevisionNumber()));
		}
		
		ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>> resources = new ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>(
				wrappers);
		
		return new ResponseEntity<ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>>(resources, HttpStatus.OK);
	}
	
	@Override
	protected QuickFilter toFilter(MultiValueMap<String, Object> parameters) {
		QuickFilter filter = new QuickFilter();
		filter.setText((String)parameters.toSingleValueMap().get("text"));
		return filter;
	}
}
