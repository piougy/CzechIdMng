package eu.bcvsolutions.idm.core.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.api.service.AuditService;
import eu.bcvsolutions.idm.core.api.service.EntityLookupService;
import eu.bcvsolutions.idm.core.model.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.core.model.dto.TreeNodeFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.repository.processor.RevisionAssembler;
import eu.bcvsolutions.idm.core.model.service.IdmTreeNodeService;

/**
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + BaseEntityController.TREE_BASE_PATH + "Nodes")
public class IdmTreeNodeController extends DefaultReadWriteEntityController<IdmTreeNode, TreeNodeFilter> {
	
	@Autowired
	private IdmTreeNodeService treeNodeService;
	
	@Autowired
	private AuditService auditService; 
	
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
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "{treeNodeId}/revisions/{revId}", method = RequestMethod.GET)
	public ResponseEntity<ResourceWrapper<DefaultRevisionEntity>> findRevision(@PathVariable("treeNodeId") String treeNodeId, @PathVariable("revId") Integer revId) {
		IdmTreeNode treeNode = getEntity(treeNodeId);
		if (treeNode == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("treeNode", treeNodeId));
		}
		
		Revision<Integer, ? extends BaseEntity> revision;
		try {
			revision = this.auditService.findRevision(IdmTreeNode.class, revId, Long.parseLong(treeNodeId));
		} catch (RevisionDoesNotExistException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", revId));
		}
		
		IdmTreeNode entity = (IdmTreeNode) revision.getEntity();
		RevisionAssembler<IdmTreeNode> assembler = new RevisionAssembler<IdmTreeNode>();
		ResourceWrapper<DefaultRevisionEntity> resource = assembler.toResource(this.getClass(),
				String.valueOf(entity.getId()), revision, revId);

		return new ResponseEntity<ResourceWrapper<DefaultRevisionEntity>>(resource, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "{treeNodeId}/revisions", method = RequestMethod.GET)
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>> findRevisions(@PathVariable("treeNodeId") String treeNodeId) {
		IdmTreeNode treeNode = getEntity(treeNodeId);
		if (treeNode == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("treeNode", treeNodeId));
		}
		
		List<ResourceWrapper<DefaultRevisionEntity>> wrappers = new ArrayList<>();
		List<Revision<Integer, ? extends BaseEntity>> revisions = this.auditService.findRevisions(IdmTreeNode.class, Long.parseLong(treeNodeId));
		try {
			revisions = this.auditService.findRevisions(IdmTreeNode.class, Long.parseLong(treeNodeId));
		} catch (RevisionDoesNotExistException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", treeNodeId));
		}
		
		RevisionAssembler<IdmTreeNode> assembler = new RevisionAssembler<IdmTreeNode>();
		
		revisions.forEach(revision -> {
			wrappers.add(assembler.toResource(this.getClass(), 
					String.valueOf(revision.getEntity().getId()),
					revision, revision.getRevisionNumber()));
		});
		
		ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>> resources = new ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>(
				wrappers);
		
		return new ResponseEntity<ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>>(resources, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/search/roots", method = RequestMethod.GET)
	public Resources<?> findRoots(@Param(value = "treeType") @Null Long treeType, PersistentEntityResourceAssembler assembler) {	
		List<IdmTreeNode> listOfRoots = this.treeNodeService.findRoots(treeType);
		
		return toResources((Iterable<?>) listOfRoots, assembler, IdmTreeNode.class, null);
	}
	
	@RequestMapping(value = "/search/children", method = RequestMethod.GET)
	public Resources<?> findChildren(@Param(value = "parent") @NotNull Long parent, PersistentEntityResourceAssembler assembler) {	
		List<IdmTreeNode> listOfChildren = this.treeNodeService.findChildrenByParent(parent);

		return toResources((Iterable<?>) listOfChildren, assembler, IdmTreeNode.class, null);
	}
	
	@Override
	protected TreeNodeFilter toFilter(MultiValueMap<String, Object> parameters) {
		TreeNodeFilter filter = new TreeNodeFilter();
		filter.setText((String)parameters.toSingleValueMap().get("text"));
		filter.setTreeNode(convertLongParameter(parameters, "parent"));
		filter.setTreeType(convertLongParameter(parameters, "treeType"));
		return filter;
	}
}
