package eu.bcvsolutions.idm.core.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.activiti.engine.runtime.ProcessInstance;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.forest.index.service.api.ForestContentService;
import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.WorkPosition;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityRoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuditService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.service.GrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;

/**
 * Rest methods for IdmIdentity resource
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/identities")
public class IdmIdentityController extends DefaultReadWriteEntityController<IdmIdentity, IdentityFilter> {

	private final GrantedAuthoritiesFactory grantedAuthoritiesFactory;
	private final IdmIdentityContractService identityContractService;
	private final IdmIdentityRoleService identityRoleService;
	private final WorkflowTaskInstanceService workflowTaskInstanceService;	
	private final WorkflowProcessInstanceService workflowProcessInstanceService;
	private final IdmAuditService auditService; 	
	private final ForestContentService<IdmTreeNode, IdmForestIndexEntity, UUID> treeNodeService;
	//
	private final IdmFormDefinitionController formDefinitionController;
	
	@Autowired
	public IdmIdentityController(
			EntityLookupService entityLookupService, 
			IdmFormDefinitionController formDefinitionController,
			GrantedAuthoritiesFactory grantedAuthoritiesFactory,
			IdmIdentityContractService identityContractService,
			IdmIdentityRoleService identityRoleService,
			WorkflowTaskInstanceService workflowTaskInstanceService,
			WorkflowProcessInstanceService workflowProcessInstanceService,
			IdmAuditService auditService,
			ForestContentService<IdmTreeNode, IdmForestIndexEntity, UUID> treeNodeService) {
		super(entityLookupService);
		//
		Assert.notNull(formDefinitionController);
		Assert.notNull(grantedAuthoritiesFactory);
		Assert.notNull(identityContractService);
		Assert.notNull(identityRoleService);
		Assert.notNull(workflowTaskInstanceService);
		Assert.notNull(workflowProcessInstanceService);
		Assert.notNull(auditService);
		Assert.notNull(treeNodeService);
		//
		this.formDefinitionController = formDefinitionController;
		this.grantedAuthoritiesFactory = grantedAuthoritiesFactory;
		this.identityContractService = identityContractService;
		this.identityRoleService = identityRoleService;
		this.workflowTaskInstanceService = workflowTaskInstanceService;
		this.workflowProcessInstanceService = workflowProcessInstanceService;
		this.auditService = auditService;
		this.treeNodeService = treeNodeService;
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.IDENTITY_WRITE + "')")
	public ResponseEntity<?> post(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.post(nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.IDENTITY_WRITE + "')")
	public ResponseEntity<?> put(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.put(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.IDENTITY_WRITE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
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
	@ResponseBody
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
	@ResponseBody
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
	
	@ResponseBody
	@RequestMapping(value = "/{identityId}/roles", method = RequestMethod.GET)
	public Resources<?> roles(@PathVariable String identityId, PersistentEntityResourceAssembler assembler) {	
		IdmIdentity identity = getEntity(identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", identityId));
		}
		// TODO: pageable support 
		IdentityRoleFilter filter = new IdentityRoleFilter();
		filter.setIdentityId(identity.getId());
		return toResources((Iterable<?>) identityRoleService.find(filter, null), assembler, IdmIdentityRole.class, null);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{identityId}/identity-contracts", method = RequestMethod.GET)
	public Resources<?> workingPositions(@PathVariable String identityId, PersistentEntityResourceAssembler assembler) {	
		IdmIdentity identity = getEntity(identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", identityId));
		}		
		return toResources((Iterable<?>) identityContractService.getContracts(identity), assembler, IdmIdentityContract.class, null);
	}
	
	/**
	 * Get given identity's main position in organization.
	 * 
	 * @param identityId
	 * @param assembler
	 * @return Positions from root to closest parent
	 */
	@ResponseBody
	@RequestMapping(value = "/{identityId}/work-position", method = RequestMethod.GET)
	public ResponseEntity<?> organizationPosition(@PathVariable String identityId, PersistentEntityResourceAssembler assembler) {
		IdmIdentity identity = getEntity(identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", identityId));
		}
		IdmIdentityContract primeContract = identityContractService.getPrimeContract(identity);
		if (primeContract == null) {
			return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
		}
		WorkPosition position = new WorkPosition(identity, primeContract);
		if (primeContract.getWorkingPosition() != null) {
			List<IdmTreeNode> positions = new ArrayList<>();
			positions = treeNodeService.findAllParents(primeContract.getWorkingPosition(), new Sort(Direction.ASC, "forestIndex.lft"));
			positions.add(primeContract.getWorkingPosition());
			position.setPath(positions);
		}		
		return new ResponseEntity<WorkPosition>(position, HttpStatus.OK);
	}
	
	@ResponseBody
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
	
	@ResponseBody
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
	
	/**
	 * Returns form definition to given identity.
	 * 
	 * @param backendId
	 * @param assembler
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definition", method = RequestMethod.GET)
	public ResponseEntity<?> getFormDefinition(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		return formDefinitionController.getDefinition(IdmIdentity.class, assembler);
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
	public Resources<?> getFormValues(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		IdmIdentity entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		return formDefinitionController.getFormValues(entity, null, assembler);
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
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.IDENTITY_WRITE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.POST)
	public Resources<?> saveFormValues(
			@PathVariable @NotNull String backendId,
			@RequestBody @Valid List<IdmIdentityFormValue> formValues,
			PersistentEntityResourceAssembler assembler) {		
		IdmIdentity entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		return formDefinitionController.saveFormValues(entity, null, formValues, assembler);
	}	
	
	@Override
	protected IdentityFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdentityFilter filter = new IdentityFilter();
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setSubordinatesFor(getParameterConverter().toEntity(parameters, "subordinatesFor", IdmIdentity.class));
		filter.setSubordinatesByTreeType(getParameterConverter().toEntity(parameters, "subordinatesByTreeType", IdmTreeType.class));
		filter.setManagersFor(getParameterConverter().toEntity(parameters, "managersFor", IdmIdentity.class));
		filter.setManagersByTreeType(getParameterConverter().toEntity(parameters, "managersByTreeType", IdmTreeType.class));
		filter.setManagersByTreeNode(getParameterConverter().toEntity(parameters, "managersByTreeNode", IdmTreeNode.class));
		filter.setTreeNode(getParameterConverter().toEntity(parameters, "treeNodeId", IdmTreeNode.class));
		filter.setRecursively(getParameterConverter().toBoolean(parameters, "recursively", true));
		filter.setTreeTypeId(getParameterConverter().toUuid(parameters, "treeTypeId"));
		// TODO: or / and in multivalues? OR is supported now
		if (parameters.containsKey("role")) {
			for(Object role : parameters.get("role")) {
				filter.getRoles().add(getParameterConverter().toEntity((String)role, IdmRole.class));
			}
		}
		return filter;
	}
}
