package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.activiti.engine.runtime.ProcessInstance;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
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
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.IdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuditService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.security.api.domain.Enabled;
import eu.bcvsolutions.idm.security.service.GrantedAuthoritiesFactory;

/**
 * Rest methods for IdmIdentity resource
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/identities")
public class IdmIdentityController extends DefaultReadWriteEntityController<IdmIdentity, IdentityFilter> {

	private final GrantedAuthoritiesFactory grantedAuthoritiesFactory;
	private final IdmIdentityContractService identityContractService;
	private final WorkflowTaskInstanceService workflowTaskInstanceService;	
	private final WorkflowProcessInstanceService workflowProcessInstanceService;
	private final IdmAuditService auditService; 	
	private final FormService formService;
	
	@Autowired 
	private IdmFormDefinitionController formDefinitionController; // TODO: is used for serialize to json only => should be removed
	
	@Autowired
	public IdmIdentityController(
			EntityLookupService entityLookupService, 
			FormService formService,
			GrantedAuthoritiesFactory grantedAuthoritiesFactory,
			IdmIdentityContractService identityContractService,
			WorkflowTaskInstanceService workflowTaskInstanceService,
			WorkflowProcessInstanceService workflowProcessInstanceService,
			IdmAuditService auditService) {
		super(entityLookupService);
		//
		Assert.notNull(formService);
		Assert.notNull(grantedAuthoritiesFactory);
		Assert.notNull(identityContractService);
		Assert.notNull(workflowTaskInstanceService);
		Assert.notNull(workflowProcessInstanceService);
		Assert.notNull(auditService);
		//
		this.formService = formService;
		this.grantedAuthoritiesFactory = grantedAuthoritiesFactory;
		this.identityContractService = identityContractService;
		this.workflowTaskInstanceService = workflowTaskInstanceService;
		this.workflowProcessInstanceService = workflowProcessInstanceService;
		this.auditService = auditService;
	}
	
	IdmIdentityService getIdentityService() {
		return (IdmIdentityService)getEntityService();
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
	
	@Override
	@Enabled(property = IdentityConfiguration.PROPERTY_IDENTITY_DELETE)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.IDENTITY_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	/**
	 * Returns given identity's granted authorities
	 * 
	 * @param identityId
	 * @return list of granted authorities
	 */
	@RequestMapping(value = "/{identityId}/authorities", method = RequestMethod.GET)
	public List<? extends GrantedAuthority> getGrantedAuthotrities(@PathVariable String identityId) {
		IdmIdentity identity = getEntity(identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", identityId));
		}
		//
		return grantedAuthoritiesFactory.getGrantedAuthorities(identity.getUsername());
	}

	/**
	 * Change given identity's permissions (assigned roles)
	 * @param identityId
	 * @return Instance of workflow user task, where applicant can fill his change permission request
	 */
	@RequestMapping(value = "/{identityId}/change-permissions", method = RequestMethod.PUT)
	public ResponseEntity<ResourceWrapper<WorkflowTaskInstanceDto>> changePermissions(@PathVariable String identityId) {	
		IdmIdentity identity = getEntity(identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", identityId));
		}
		ProcessInstance processInstance = workflowProcessInstanceService.startProcess(IdmIdentityService.ADD_ROLE_TO_IDENTITY_WORKFLOW,
				IdmIdentity.class.getSimpleName(), identity.getUsername(), identity.getId().toString(), null);
		//
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessInstanceId(processInstance.getId());
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService.search(filter).getResources();
		return new ResponseEntity<ResourceWrapper<WorkflowTaskInstanceDto>>(new ResourceWrapper<WorkflowTaskInstanceDto>(tasks.get(0)), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{identityId}/roles", method = RequestMethod.GET)
	public Resources<?> roles(@PathVariable String identityId, PersistentEntityResourceAssembler assembler) {	
		IdmIdentity identity = getEntity(identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", identityId));
		}
		// TODO: IdmIdentityRoleService and pagination support?
		return toResources((Iterable<?>) identity.getRoles(), assembler, IdmIdentityRole.class, null);
	}
	
	@RequestMapping(value = "/{identityId}/identity-contracts", method = RequestMethod.GET)
	public Resources<?> workingPositions(@PathVariable String identityId, PersistentEntityResourceAssembler assembler) {	
		IdmIdentity identity = getEntity(identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", identityId));
		}		
		return toResources((Iterable<?>) identityContractService.getContracts(identity), assembler, IdmIdentityContract.class, null);
	}
	
	@RequestMapping(value = "/{identityId}/revisions/{revId}", method = RequestMethod.GET)
	public ResponseEntity<?> findRevision(@PathVariable("identityId") String identityId, @PathVariable("revId") Long revId, PersistentEntityResourceAssembler assembler) {
		IdmIdentity originalEntity = getEntity(identityId);
		if (originalEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", identityId));
		}
		
		IdmIdentity revisionIdentity;
		try {
			revisionIdentity = this.auditService.findRevision(IdmIdentity.class, originalEntity.getId(), revId);
		} catch (RevisionDoesNotExistException ex) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", revId), ex);
		}

		return new ResponseEntity<>(toResource(revisionIdentity, assembler), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{identityId}/revisions", method = RequestMethod.GET)
	public Resources<?> findRevisions(@PathVariable("identityId") String identityId, Pageable pageable,
			PersistentEntityResourceAssembler assembler) {
		IdmIdentity originalEntity = getEntity(identityId);
		if (originalEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("identity", identityId));
		}
		
		// get original entity id
		Page<IdmAudit> results = this.auditService.getRevisionsForEntity(IdmIdentity.class.getSimpleName(), originalEntity.getId(), pageable);
		
		return toResources(results, assembler, IdmAudit.class, null);
	}
	
	@Override
	protected IdentityFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdentityFilter filter = new IdentityFilter();
		filter.setText(convertStringParameter(parameters, "text"));
		filter.setSubordinatesFor(convertEntityParameter(parameters, "subordinatesFor", IdmIdentity.class));
		filter.setSubordinatesByTreeType(convertEntityParameter(parameters, "subordinatesByTreeType", IdmTreeType.class));
		filter.setManagersFor(convertEntityParameter(parameters, "managersFor", IdmIdentity.class));
		filter.setManagersByTreeType(convertEntityParameter(parameters, "managersByTreeType", IdmTreeType.class));
		filter.setManagersByTreeNode(convertEntityParameter(parameters, "managersByTreeNode", IdmTreeNode.class));
		// TODO: or / and in multivalues? OR is supported now
		if (parameters.containsKey("role")) {
			for(Object role : parameters.get("role")) {
				filter.getRoles().add(convertEntityParameter((String)role, IdmRole.class));
			}
		}
		return filter;
	}
	
	/**
	 * Returns form definition to given identity.
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@RequestMapping(value = "/{backendId}/form-definition", method = RequestMethod.GET)
	public ResponseEntity<?> getFormDefinition(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		IdmFormDefinition formDefinition = getFormDefinition(null);
		return formDefinitionController.get(formDefinition.getId().toString(), assembler);
	}
	
	/**
	 * Returns filled form values
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	public Resources<?> getFormValues(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		IdmIdentity identity = getEntity(backendId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinition formDefinition = getFormDefinition(identity);
		return toResources(formService.getValues(identity, formDefinition), assembler, getEntityClass(), null);
	}
	
	/**
	 * Saves connector configuration form values
	 * 
	 * @param backendId
	 * @param formValues
	 * @param assembler
	 * @return
	 */
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.IDENTITY_WRITE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.POST)
	public Resources<?> saveFormValues(
			@PathVariable @NotNull String backendId,
			@RequestBody @Valid List<IdmIdentityFormValue> formValues,
			PersistentEntityResourceAssembler assembler) {		
		IdmIdentity identity = getEntity(backendId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinition formDefinition = getFormDefinition(identity);
		formService.saveValues(identity, formDefinition, formValues);
		return getFormValues(backendId, assembler);
	}
	
	/**
	 * Returns form definition for given identity
	 * 
	 * @param identity
	 * @return
	 */
	private IdmFormDefinition getFormDefinition(IdmIdentity identity) {
		// find default definition only (maybe we will need to customize form definition to custom entity instance)
		IdmFormDefinition formDefinition = formService.getDefinition(IdmIdentity.class.getCanonicalName(), null);
		if (formDefinition == null) {			
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("formDefinition", IdmIdentity.class.getCanonicalName()));
		}			
		return formDefinition;	
	}
}
