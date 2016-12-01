package eu.bcvsolutions.idm.core.rest.impl;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.TreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAudit;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuditService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;

/**
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + BaseEntityController.TREE_BASE_PATH + "-nodes")
public class IdmTreeNodeController extends DefaultReadWriteEntityController<IdmTreeNode, TreeNodeFilter> {
	
	@Autowired
	private IdmTreeNodeService treeNodeService;
	
	@Autowired
	private IdmAuditService auditService; 
	
	@Autowired
	public IdmTreeNodeController(EntityLookupService entityLookupService, IdmTreeNodeService treeNodeService) {
		super(entityLookupService, treeNodeService);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.TREENODE_WRITE + "')")
	public ResponseEntity<?> create(HttpServletRequest nativeRequest, PersistentEntityResourceAssembler assembler)
			throws HttpMessageNotReadableException {
		return super.create(nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.TREENODE_WRITE + "')")
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.TREENODE_WRITE + "')")
	public ResponseEntity<?> update(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest,
			PersistentEntityResourceAssembler assembler) throws HttpMessageNotReadableException {
		return super.update(backendId, nativeRequest, assembler);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.TREENODE_DELETE + "')")
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@RequestMapping(value = "{treeNodeId}/revisions/{revId}", method = RequestMethod.GET)
	public ResponseEntity<?> findRevision(@PathVariable("treeNodeId") String treeNodeId, @PathVariable("revId") Long revId, PersistentEntityResourceAssembler assembler) {
		IdmTreeNode treeNode = getEntity(treeNodeId);
		if (treeNode == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("treeNode", treeNodeId));
		}
		
		IdmTreeNode revision;
		try {
			revision = this.auditService.findRevision(IdmTreeNode.class, treeNode.getId(), revId);
		} catch (RevisionDoesNotExistException ex) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", revId), ex);
		}

		return new ResponseEntity<>(toResource(revision, assembler), HttpStatus.OK);
	}

	@RequestMapping(value = "{treeNodeId}/revisions", method = RequestMethod.GET)
	public Resources<?> findRevisions(@PathVariable("treeNodeId") String treeNodeId, Pageable pageable, 
			PersistentEntityResourceAssembler assembler) {
		IdmTreeNode treeNode = getEntity(treeNodeId);
		if (treeNode == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("treeNode", treeNodeId));
		}
		Page<IdmAudit> results = this.auditService.getRevisionsForEntity(IdmTreeNode.class.getSimpleName(), UUID.fromString(treeNodeId), pageable);
		return toResources(results, assembler, IdmRole.class, null);
	}
	
	@RequestMapping(value = "/search/roots", method = RequestMethod.GET)
	public Resources<?> findRoots(
			@RequestParam(value = "treeType", required = false) String treeType,
			PersistentEntityResourceAssembler assembler,
			@PageableDefault Pageable pageable) {
		// TODO: try - catch ... or better findRoots with tree type instance (aditional select, but type save)
		Page<IdmTreeNode> roots = this.treeNodeService.findRoots(UUID.fromString(treeType), pageable);
		return toResources(roots, assembler, IdmTreeNode.class, null);
	}
	
	@RequestMapping(value = "/search/children", method = RequestMethod.GET)
	public Resources<?> findChildren(
			@RequestParam(value = "parent") @NotNull String parent, 
			PersistentEntityResourceAssembler assembler,
			@PageableDefault Pageable pageable) {	
		// TODO: try - catch ... or better findChildrenByParent with tree node instance (aditional select, but type save)
		Page<IdmTreeNode> children = this.treeNodeService.findChildrenByParent(UUID.fromString(parent), pageable);
		return toResources(children, assembler, IdmTreeNode.class, null);
	}
	
	@Override
	protected TreeNodeFilter toFilter(MultiValueMap<String, Object> parameters) {
		TreeNodeFilter filter = new TreeNodeFilter();
		filter.setText((String)parameters.toSingleValueMap().get("text"));
		filter.setTreeNodeId(convertUuidParameter(parameters, "parent"));
		filter.setTreeTypeId(convertUuidParameter(parameters, "treeType"));
		return filter;
	}
}
