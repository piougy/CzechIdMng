package eu.bcvsolutions.idm.core.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
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

import eu.bcvsolutions.forest.index.service.api.ForestContentService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteEntityController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.model.dto.WorkPositionDto;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityRoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuditService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.service.GrantedAuthoritiesFactory;

/**
 * Rest methods for IdmIdentity resource
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/identities")
public class IdmIdentityController extends AbstractReadWriteEntityController<IdmIdentity, IdentityFilter> {

	private final GrantedAuthoritiesFactory grantedAuthoritiesFactory;
	private final IdmIdentityContractService identityContractService;
	private final IdmIdentityRoleService identityRoleService;
	private final IdmAuditService auditService; 	
	private final IdmTreeNodeService treeNodeService;
	private final ForestContentService<IdmTreeNode, IdmForestIndexEntity, UUID> treeNodeIndexService;
	//
	private final IdmFormDefinitionController formDefinitionController;
	
	@Autowired
	public IdmIdentityController(
			EntityLookupService entityLookupService, 
			IdmFormDefinitionController formDefinitionController,
			GrantedAuthoritiesFactory grantedAuthoritiesFactory,
			IdmIdentityContractService identityContractService,
			IdmIdentityRoleService identityRoleService,
			IdmAuditService auditService,
			ForestContentService<IdmTreeNode, IdmForestIndexEntity, UUID> treeNodeIndexService,
			IdmTreeNodeService treeNodeService) {
		super(entityLookupService);
		//
		Assert.notNull(formDefinitionController);
		Assert.notNull(grantedAuthoritiesFactory);
		Assert.notNull(identityContractService);
		Assert.notNull(identityRoleService);
		Assert.notNull(auditService);
		Assert.notNull(treeNodeIndexService);
		Assert.notNull(treeNodeService);
		//
		this.formDefinitionController = formDefinitionController;
		this.grantedAuthoritiesFactory = grantedAuthoritiesFactory;
		this.identityContractService = identityContractService;
		this.identityRoleService = identityRoleService;
		this.auditService = auditService;
		this.treeNodeIndexService = treeNodeIndexService;
		this.treeNodeService = treeNodeService;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	public Resources<?> find(@RequestParam MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.find(parameters, pageable, assembler);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_AUTOCOMPLETE + "')")
	public Resources<?> autocomplete(@RequestParam MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable, 			
			PersistentEntityResourceAssembler assembler) {
		return super.autocomplete(parameters, pageable, assembler);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	public ResponseEntity<?> get(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		return super.get(backendId, assembler);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_CREATE + "') or hasAuthority('" + CoreGroupPermission.IDENTITY_UPDATE + "')")
	public ResponseEntity<?> post(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.post(nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_UPDATE + "')")
	public ResponseEntity<?> put(
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.put(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_UPDATE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler) 
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_DELETE + "')")
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	public List<? extends GrantedAuthority> getGrantedAuthotrities(@PathVariable String identityId) {
		IdmIdentity identity = getEntity(identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", identityId));
		}
		checkAccess(identity, IdmBasePermission.READ);
		//
		return grantedAuthoritiesFactory.getGrantedAuthorities(identity.getUsername());
	}
	
	@ResponseBody
	@RequestMapping(value = "/{identityId}/roles", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	public Resources<?> roles(@PathVariable String identityId, PersistentEntityResourceAssembler assembler) {	
		IdmIdentity identity = getEntity(identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", identityId));
		}
		//
		checkAccess(identity, IdmBasePermission.READ);
		//
		IdentityRoleFilter filter = new IdentityRoleFilter();
		filter.setIdentityId(identity.getId());		
		Page<IdmIdentityRoleDto> identityRoles = identityRoleService.find(filter, null);
		//
		return entitiesToResources((Page)identityRoles, null, IdmIdentityRoleDto.class, null);
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	public ResponseEntity<?> organizationPosition(@PathVariable String identityId, PersistentEntityResourceAssembler assembler) {
		IdmIdentity identity = getEntity(identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", identityId));
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
	@RequestMapping(value = "/{identityId}/revisions/{revId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	public ResponseEntity<?> findRevision(@PathVariable("identityId") String identityId, @PathVariable("revId") Long revId, PersistentEntityResourceAssembler assembler) {
		IdmIdentity originalEntity = getEntity(identityId);
		if (originalEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", identityId));
		}
		checkAccess(originalEntity, IdmBasePermission.READ);
		//
		IdmIdentity revisionIdentity;
		try {
			revisionIdentity = this.auditService.findRevision(IdmIdentity.class, originalEntity.getId(), revId);
			checkAccess(revisionIdentity, IdmBasePermission.READ);
		} catch (RevisionDoesNotExistException ex) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", revId), ex);
		}

		return new ResponseEntity<>(toResource(revisionIdentity, assembler), HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{identityId}/revisions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	public Resources<?> findRevisions(@PathVariable("identityId") String identityId, Pageable pageable,
			PersistentEntityResourceAssembler assembler) {
		IdmIdentity originalEntity = getEntity(identityId);
		if (originalEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("identity", identityId));
		}
		//
		checkAccess(originalEntity, IdmBasePermission.READ);
		// get original entity id
		Page<IdmAudit> results = this.auditService.getRevisionsForEntity(IdmIdentity.class.getSimpleName(), originalEntity.getId(), pageable);
		//
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_READ + "')")
	public Resources<?> getFormValues(@PathVariable @NotNull String backendId, PersistentEntityResourceAssembler assembler) {
		IdmIdentity entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		checkAccess(entity, IdmBasePermission.READ);
		//
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.POST)
	public Resources<?> saveFormValues(
			@PathVariable @NotNull String backendId,
			@RequestBody @Valid List<IdmIdentityFormValue> formValues,
			PersistentEntityResourceAssembler assembler) {		
		IdmIdentity entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(entity, IdmBasePermission.UPDATE);
		//
		return formDefinitionController.saveFormValues(entity, null, formValues, assembler);
	}	
	
	@Override
	protected IdentityFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdentityFilter filter = new IdentityFilter(parameters);
		filter.setId(getParameterConverter().toUuid(parameters, "id"));
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setSubordinatesFor(getParameterConverter().toEntity(parameters, IdentityFilter.PARAMETER_SUBORDINATES_FOR, IdmIdentity.class));
		filter.setSubordinatesByTreeType(getParameterConverter().toEntity(parameters, IdentityFilter.PARAMETER_SUBORDINATES_BY_TREE_TYPE, IdmTreeType.class));
		filter.setManagersFor(getParameterConverter().toEntity(parameters, IdentityFilter.PARAMETER_MANAGERS_FOR, IdmIdentity.class));
		filter.setManagersByTreeType(getParameterConverter().toEntity(parameters, IdentityFilter.PARAMETER_MANAGERS_BY_TREE_TYPE, IdmTreeType.class));
		filter.setTreeNode(getParameterConverter().toEntity(parameters, "treeNodeId", IdmTreeNode.class));
		filter.setRecursively(getParameterConverter().toBoolean(parameters, "recursively", true));
		filter.setTreeTypeId(getParameterConverter().toUuid(parameters, "treeTypeId"));
		filter.setManagersByContractId(getParameterConverter().toUuid(parameters, IdentityFilter.PARAMETER_MANAGERS_BY_CONTRACT_ID));
		filter.setIncludeGuarantees(getParameterConverter().toBoolean(parameters, "includeGuarantees", false));
		// TODO: or / and in multivalues? OR is supported now
		if (parameters.containsKey("role")) {
			for(Object role : parameters.get("role")) {
				filter.getRoles().add(getParameterConverter().toEntity((String)role, IdmRole.class));
			}
		}
		return filter;
	}
}
