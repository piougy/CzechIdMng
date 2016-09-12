package eu.bcvsolutions.idm.core.rest.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.model.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeLookup;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.IdmAuditService;
import eu.bcvsolutions.idm.core.rest.IdmTreeNodeController;
import eu.bcvsolutions.idm.core.model.repository.processor.RevisionAssembler;

/**
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RestController
@RequestMapping(value = "/api/treenodes/")
public class DefaultIdmTreeNodeController implements IdmTreeNodeController {
	
	@Autowired
	private IdmTreeNodeLookup treeNodeLookup;
	
	@Autowired
	private IdmTreeNodeRepository treeNodeRepository;
	
	@Autowired
	private IdmAuditService auditService; 
	
	@Override
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "{treeNodeId}/revisions/{revId}", method = RequestMethod.GET)
	public ResponseEntity<ResourceWrapper<DefaultRevisionEntity>> findRevision(@PathVariable("treeNodeId") String treeNodeId, @PathVariable("revId") Integer revId) {
		IdmTreeNode treeNode = treeNodeRepository.findOne(Long.parseLong(treeNodeId));
		if (treeNode == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("treeNode", treeNodeId));
		}
		
		Revision<Integer, ? extends AbstractEntity> revision;
		try {
			revision = this.auditService.findRevision(IdmTreeNode.class, revId, Long.parseLong(treeNodeId));
		} catch (RevisionDoesNotExistException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", revId));
		}
		
		IdmTreeNode entity = (IdmTreeNode) revision.getEntity();
		RevisionAssembler<IdmTreeNode> assembler = new RevisionAssembler<IdmTreeNode>();
		ResourceWrapper<DefaultRevisionEntity> resource = assembler.toResource(this.getClass(),
				String.valueOf(this.treeNodeLookup.getResourceIdentifier(entity)), revision, revId);

		return new ResponseEntity<ResourceWrapper<DefaultRevisionEntity>>(resource, HttpStatus.OK);
	}

	@Override
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "{treeNodeId}/revisions", method = RequestMethod.GET)
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>> findRevisions(@PathVariable("treeNodeId") String treeNodeId) {
		IdmTreeNode treeNode = treeNodeRepository.findOne(Long.parseLong(treeNodeId));
		if (treeNode == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("treeNode", treeNodeId));
		}
		
		List<ResourceWrapper<DefaultRevisionEntity>> wrappers = new ArrayList<>();
		List<Revision<Integer, ? extends AbstractEntity>> revisions = this.auditService.findRevisions(IdmTreeNode.class, Long.parseLong(treeNodeId));
		try {
			revisions = this.auditService.findRevisions(IdmTreeNode.class, Long.parseLong(treeNodeId));
		} catch (RevisionDoesNotExistException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", treeNodeId));
		}
		
		RevisionAssembler<IdmTreeNode> assembler = new RevisionAssembler<IdmTreeNode>();
		
		for	(Revision<Integer, ? extends AbstractEntity> revision : revisions) {
			wrappers.add(assembler.toResource(this.getClass(), 
					String.valueOf(this.treeNodeLookup.getResourceIdentifier((IdmTreeNode)revision.getEntity())),
					revision, revision.getRevisionNumber()));
		}
		
		ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>> resources = new ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>(
				wrappers);
		
		return new ResponseEntity<ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>>(resources, HttpStatus.OK);
	}

}
