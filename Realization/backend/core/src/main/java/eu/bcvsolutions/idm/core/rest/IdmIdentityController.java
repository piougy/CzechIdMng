package eu.bcvsolutions.idm.core.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.RestApplicationException;
import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityLookup;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.service.IdmAuditService;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.revision.IdmRevisionController;
import eu.bcvsolutions.idm.core.revision.RevisionAssembler;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.rest.WorkflowTaskInstanceController;
import eu.bcvsolutions.idm.security.service.GrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.security.service.SecurityService;

@RestController
@RequestMapping(value = "/api/")
public class IdmIdentityController implements IdmRevisionController  {
	
	public static final String ENDPOINT_NAME = "identities";

	@Autowired
	private IdmIdentityRepository identityRepository;

	@Autowired
	private IdmIdentityLookup identityLookup;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private GrantedAuthoritiesFactory grantedAuthoritiesFactory;

	@Autowired
	private IdmIdentityService idmIdentityService;

	@Autowired
	private WorkflowTaskInstanceController workflowTaskInstanceController;
	
	@Autowired
	private IdmAuditService auditService; 

	/**
	 * Changes identity password. Could be public, because previous password is required.
	 * 
	 * @param identityId
	 * @param passwordChangeDto
	 * @return
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "public/" + ENDPOINT_NAME + "/{identityId}/password-change", method = RequestMethod.PUT)
	public ResponseEntity<Void> passwordChange(@PathVariable String identityId,
			@RequestBody @Valid PasswordChangeDto passwordChangeDto) {
		IdmIdentity identity = (IdmIdentity) identityLookup.lookupEntity(identityId);
		if (identity == null) {
			throw new RestApplicationException(CoreResultCode.NOT_FOUND, ImmutableMap.of("identity", identityId));
		}
		// TODO: hasAnyAuthority as permissionGroup
		if (!securityService.hasAnyAuthority("SYSTEM_ADMIN") && !StringUtils.equals(new String(identity.getPassword()),
				new String(passwordChangeDto.getOldPassword()))) {
			throw new RestApplicationException(CoreResultCode.PASSWORD_CHANGE_CURRENT_FAILED_IDM);
		}
		identity.setPassword(passwordChangeDto.getNewPassword());
		identityRepository.save(identity);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Returns given identity's granted authorities
	 * 
	 * @param identityId
	 * @return list of granted authorities
	 */
	@RequestMapping(value = ENDPOINT_NAME + "/{identityId}/authorities", method = RequestMethod.GET)
	public List<? extends GrantedAuthority> getGrantedAuthotrities(@PathVariable String identityId) {
		return grantedAuthoritiesFactory.getGrantedAuthorities(identityId);
	}

	/**
	 * Change given identity's permissions (assigned roles)
	 * @param identityId
	 * @return Instance of workflow user task, where applicant can fill his change permission request
	 */
	@RequestMapping(value = ENDPOINT_NAME + "/{identityId}/change-permissions", method = RequestMethod.PUT)
	public ResponseEntity<ResourceWrapper<WorkflowTaskInstanceDto>> changePermissions(@PathVariable String identityId) {	
		IdmIdentity identity = (IdmIdentity) identityLookup.lookupEntity(identityId);
		if (identity == null) {
			throw new RestApplicationException(CoreResultCode.NOT_FOUND, ImmutableMap.of("identity", identityId));
		}
		ProcessInstance processInstance = idmIdentityService.changePermissions(identity);
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessInstanceId(processInstance.getId());
		List<ResourceWrapper<WorkflowTaskInstanceDto>> tasks = (List<ResourceWrapper<WorkflowTaskInstanceDto>>) workflowTaskInstanceController
				.search(filter).getBody().getResources();
		return new ResponseEntity<ResourceWrapper<WorkflowTaskInstanceDto>>(tasks.get(0), HttpStatus.OK);
	}
	
	@Override
	@RequestMapping(value = ENDPOINT_NAME + "/{identityId}/revisions/{revId}", method = RequestMethod.GET)
	public ResponseEntity<ResourceWrapper<DefaultRevisionEntity>> findRevision(@PathVariable("identityId") String identityId, @PathVariable("revId") Integer revId) {
		IdmIdentity originalEntity = this.identityLookup.findOneByName(identityId);
		if (originalEntity == null) {
			throw new RestApplicationException(CoreResultCode.NOT_FOUND, ImmutableMap.of("identity", identityId));
		}
		
		Revision<Integer, ? extends AbstractEntity> revision;
		try {
			revision = this.auditService.findRevision(IdmIdentity.class, revId, originalEntity.getId());
		} catch (RevisionDoesNotExistException e) {
			throw new RestApplicationException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", revId));
		}
		
		IdmIdentity entity = (IdmIdentity) revision.getEntity();
		RevisionAssembler<IdmIdentity> assembler = new RevisionAssembler<IdmIdentity>();
		ResourceWrapper<DefaultRevisionEntity> resource = assembler.toResource(this.getClass(),
				String.valueOf(this.identityLookup.getResourceIdentifier(entity)), revision, revId);

		return new ResponseEntity<ResourceWrapper<DefaultRevisionEntity>>(resource, HttpStatus.OK);
	}

	@Override
	@RequestMapping(value = ENDPOINT_NAME + "/{identityId}/revisions", method = RequestMethod.GET)
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>> findRevisions(@PathVariable("identityId") String identityId) {
		IdmIdentity originalEntity = this.identityLookup.findOneByName(identityId);
		if (originalEntity == null) {
			throw new RestApplicationException(CoreResultCode.NOT_FOUND, ImmutableMap.of("identity", identityId));
		}
		
		List<ResourceWrapper<DefaultRevisionEntity>> wrappers = new ArrayList<>();
		List<Revision<Integer, ? extends AbstractEntity>> revisions;
		try {
			revisions = this.auditService.findRevisions(IdmIdentity.class, originalEntity.getId());
		} catch (RevisionDoesNotExistException e) {
			throw new RestApplicationException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", originalEntity.getId()));
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
}
