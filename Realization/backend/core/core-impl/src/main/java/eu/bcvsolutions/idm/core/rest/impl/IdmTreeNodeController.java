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
import org.springframework.http.MediaType;
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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.dto.filter.TreeNodeFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmTreeNodeFormValue;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuditService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Tree nodes endpoint
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@RepositoryRestController
@RequestMapping(value = BaseEntityController.BASE_PATH + BaseEntityController.TREE_BASE_PATH + "-nodes")
@Api(
		value = IdmTreeNodeController.TAG,  
		tags = { IdmTreeNodeController.TAG }, 
		description = "Operation with tree nodes",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmTreeNodeController extends DefaultReadWriteEntityController<IdmTreeNode, TreeNodeFilter> {
	
	protected static final String TAG = "Tree structure - nodes";
	private final IdmTreeNodeService treeNodeService;
	private final IdmAuditService auditService;
	//
	private final IdmFormDefinitionController formDefinitionController;
	
	@Autowired
	public IdmTreeNodeController(
			LookupService entityLookupService, 
			IdmTreeNodeService treeNodeService,
			IdmAuditService auditService,
			IdmFormDefinitionController formDefinitionController) {
		super(entityLookupService, treeNodeService);
		//
		Assert.notNull(treeNodeService);
		Assert.notNull(auditService);
		Assert.notNull(formDefinitionController);
		//
		this.treeNodeService = treeNodeService;
		this.auditService = auditService;
		this.formDefinitionController = formDefinitionController;
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.TREENODE_UPDATE + "')")
	@ApiOperation(
			value = "Create / update tree node", 
			nickname = "postTreeNode", 
			response = IdmTreeNode.class, 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.post(nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_UPDATE + "')")
	@ApiOperation(
			value = "Update tree node",
			nickname = "putTreeNode", 
			response = IdmTreeNode.class, 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.put(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_UPDATE + "')")
	@ApiOperation(
			value = "Update tree node",
			nickname = "patchTreeNode", 
			response = IdmTreeNode.class, 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_DELETE + "')")
	@ApiOperation(
			value = "Delete tree node", 
			nickname = "deleteTreeNode", 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@ResponseBody
	@RequestMapping(value = "{backendId}/revisions/{revId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@ApiOperation(
			value = "Tree node audit - read revision detail", 
			nickname = "getTreeNodeRevision", 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") })
				})
	public ResponseEntity<?> findRevision(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable("backendId") String backendId, 
			@ApiParam(value = "Revision identifier.", required = true)
			@PathVariable("revId") Long revId, 
			PersistentEntityResourceAssembler assembler) {
		IdmTreeNode treeNode = getEntity(backendId);
		if (treeNode == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("treeNode", backendId));
		}
		
		IdmTreeNode revision;
		try {
			revision = this.auditService.findRevision(IdmTreeNode.class, treeNode.getId(), revId);
		} catch (RevisionDoesNotExistException ex) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", revId), ex);
		}

		return new ResponseEntity<>(toResource(revision, assembler), HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "{backendId}/revisions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@ApiOperation(
			value = "Tree node audit - read all revisions", 
			nickname = "getTreeNodeRevisions", 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") })
				})
	public Resources<?> findRevisions(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable("backendId") String backendId, 
			Pageable pageable, 
			PersistentEntityResourceAssembler assembler) {
		IdmTreeNode treeNode = getEntity(backendId);
		if (treeNode == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("treeNode", backendId));
		}
		Page<IdmAuditDto> results = this.auditService.findRevisionsForEntity(IdmTreeNode.class.getSimpleName(), UUID.fromString(backendId), pageable);
		return toResources(results, assembler, IdmTreeNode.class, null);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/roots", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search root tree nodes", 
			nickname = "searchRootTreeNodes", 
			tags = { IdmRoleCatalogueController.TAG })
	@ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "string", paramType = "query",
                value = "Results page you want to retrieve (0..N)"),
        @ApiImplicitParam(name = "size", dataType = "string", paramType = "query",
                value = "Number of records per page."),
        @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                value = "Sorting criteria in the format: property(,asc|desc). " +
                        "Default sort order is ascending. " +
                        "Multiple sort criteria are supported.")
	})
	public Resources<?> findRoots(
			@ApiParam(value = "Tree type uuid identifier.", required = false)
			@RequestParam(value = "treeTypeId", required = false) String treeTypeId,
			PersistentEntityResourceAssembler assembler,
			@PageableDefault Pageable pageable) {
		Page<IdmTreeNode> roots = this.treeNodeService.findRoots(UUID.fromString(treeTypeId), pageable);
		return toResources(roots, assembler, IdmTreeNode.class, null);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/children", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search sub tree nodes", 
			nickname = "searchChildrenTreeNodes", 
			tags = { IdmRoleCatalogueController.TAG })
	@ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "string", paramType = "query",
                value = "Results page you want to retrieve (0..N)"),
        @ApiImplicitParam(name = "size", dataType = "string", paramType = "query",
                value = "Number of records per page."),
        @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                value = "Sorting criteria in the format: property(,asc|desc). " +
                        "Default sort order is ascending. " +
                        "Multiple sort criteria are supported.")
	})
	public Resources<?> findChildren(
			@ApiParam(value = "Superior tree node's uuid identifier.", required = true)
			@RequestParam(value = "parent") @NotNull String parent, 
			PersistentEntityResourceAssembler assembler,
			@PageableDefault Pageable pageable) {	
		Page<IdmTreeNode> children = this.treeNodeService.findChildrenByParent(UUID.fromString(parent), pageable);
		return toResources(children, assembler, IdmTreeNode.class, null);
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
	@ApiOperation(
			value = "Tree node extended attributes form definitions", 
			nickname = "getTreeNodeFormDefinitions", 
			tags = { IdmTreeNodeController.TAG })
	public ResponseEntity<?> getFormDefinitions(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			PersistentEntityResourceAssembler assembler) {
		return formDefinitionController.getDefinitions(IdmTreeNode.class, assembler);
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
	@ApiOperation(
			value = "Tree node form definition - read values", 
			nickname = "getTreeNodeFormValues", 
			tags = { IdmTreeNodeController.TAG })
	public Resources<?> getFormValues(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode,
			PersistentEntityResourceAssembler assembler) {
		IdmTreeNode entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmFormDefinition formDefinition = formDefinitionController.getDefinition(IdmTreeNode.class, definitionCode);
		//
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.POST)
	@ApiOperation(
			value = "Tree node form definition - save values", 
			nickname = "postTreeNodeFormValues", 
			tags = { IdmTreeNodeController.TAG })
	public Resources<?> saveFormValues(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode,
			@RequestBody @Valid List<IdmTreeNodeFormValue> formValues,
			PersistentEntityResourceAssembler assembler) {		
		IdmTreeNode entity = getEntity(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmFormDefinition formDefinition = formDefinitionController.getDefinition(IdmTreeNode.class, definitionCode);
		//
		return formDefinitionController.saveFormValues(entity, formDefinition, formValues, assembler);
	}
	
	@Override
	protected TreeNodeFilter toFilter(MultiValueMap<String, Object> parameters) {
		TreeNodeFilter filter = new TreeNodeFilter();
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setTreeTypeId(getParameterConverter().toUuid(parameters, "treeTypeId"));
		filter.setTreeNode(getParameterConverter().toUuid(parameters, "treeNodeId"));
		filter.setDefaultTreeType(getParameterConverter().toBoolean(parameters, "defaultTreeType"));
 		filter.setRecursively(getParameterConverter().toBoolean(parameters, "recursively", true));
		return filter;
	}	
}
