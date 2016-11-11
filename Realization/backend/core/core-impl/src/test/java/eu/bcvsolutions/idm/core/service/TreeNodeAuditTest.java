package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.DefaultRevisionEntity;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.api.service.AuditService;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.core.rest.impl.IdmTreeNodeController;
import eu.bcvsolutions.idm.core.test.AbstractIntegrationTest;

/**
 * Audit for organization test
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class TreeNodeAuditTest extends AbstractIntegrationTest {
	
	@Autowired
	private IdmTreeNodeRepository treeNodeRepository;
	
	@Autowired
	private IdmTreeTypeRepository treeTypeRepository;
	
	@Autowired
	private IdmTreeNodeController treeNodeController;
	
	@Autowired
	private AuditService auditService;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	private IdmTreeType type = null;
	private IdmTreeNode node = null;
	
	private final String testTypeName = "test_audit_type";
	private final String testName = "test_audit_node";
	private final String adminModifier = "admin";
	
	@After
	@Transactional
	public void deleteNode() {
		// we need to ensure "rollback" manually the same as we are starting transaction manually		
		treeNodeRepository.delete(node);
		if(type != null) {
			treeTypeRepository.delete(type);
		}
	}
	
	@Test
	public void testCreateNode() {
		loginAsAdmin(adminModifier);
		try {
			type = saveInTransaction(constructTestType(this.testTypeName), treeTypeRepository);
			node = saveInTransaction(constructTestNode(null, type), treeNodeRepository);
	
			assertNotNull(node.getId());
			
			getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus arg0) {
					List<Revision<Integer, ? extends BaseEntity>> revisions = auditService.findRevisions(IdmTreeNode.class, node.getId());
					
					assertEquals(1, revisions.size());
					
					IdmTreeNode org = (IdmTreeNode) auditService.findRevision(IdmTreeNode.class, 
							revisions.get(revisions.size() - 1).getRevisionNumber(), node.getId()).getEntity();
					
					assertEquals(node.getId(), org.getId());
					assertEquals(node.getName(), org.getName());
					assertEquals(node.getModifier(), org.getModifier());
				}
			});
		} finally {
			logout();
		}
	}
	
	@Test
	public void testChangeName() {
		loginAsAdmin(adminModifier);
		try {
			type = constructTestType(this.testTypeName);
			saveInTransaction(type, treeTypeRepository);
			
			node = constructTestNode(null, type);
			node = saveInTransaction(node, treeNodeRepository);
			
			final String firstName = node.getName();
			
			node.setName(testName + "2");
			node = saveInTransaction(node, treeNodeRepository);
			
			final String secondName = node.getName();
			
			getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus arg0) {
					List<Revision<Integer, ? extends BaseEntity>> revisions = auditService.findRevisions(IdmTreeNode.class, node.getId());
					assertEquals(2, revisions.size());
					
					Collections.sort(revisions, new Comparator<Revision<Integer, ? extends BaseEntity>>() {
						@Override
						public int compare(Revision<Integer, ? extends BaseEntity> o1,
								Revision<Integer, ? extends BaseEntity> o2) {
							return o1.compareTo(o2);
						}
					});
					
					IdmTreeNode revisionRole = (IdmTreeNode) revisions.get(revisions.size() - 1).getEntity();
					
					assertEquals(secondName, revisionRole.getName());
					
					revisionRole = (IdmTreeNode) revisions.get(revisions.size() - 2).getEntity();
					
					assertEquals(firstName, revisionRole.getName());
				}
			});
		} finally {
			logout();
		}
	}
	
	@Test
	public void testCheckModifier() {
		type = constructTestType(this.testTypeName);
		saveInTransaction(type, treeTypeRepository);
		
		node = constructTestNode(null, type);
		node.setCode(testName + "_2");
		node.setName(testName + "_2");
		node = saveInTransaction(node, treeNodeRepository);		
		
		final String firstModifier = node.getModifier();
		
		loginAsAdmin(adminModifier);
		
		try {
			node.setName(testName + "_3");
			node = saveInTransaction(node, treeNodeRepository);
			
			final String secondModifier = node.getModifier();
			
			getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus arg0) {
					UUID id = node.getId();
					
					List<Revision<Integer, ? extends BaseEntity>> revisions = auditService.findRevisions(IdmTreeNode.class, id);
					assertEquals(2, revisions.size());
					
					Collections.sort(revisions, new Comparator<Revision<Integer, ? extends BaseEntity>>() {
						@Override
						public int compare(Revision<Integer, ? extends BaseEntity> o1,
								Revision<Integer, ? extends BaseEntity> o2) {
							return o1.compareTo(o2);
						}
					});
					
					IdmTreeNode revisionOrganization = (IdmTreeNode) revisions.get(revisions.size() - 2).getEntity();
					
					assertEquals(firstModifier, revisionOrganization.getModifier());
					
					revisionOrganization = (IdmTreeNode) revisions.get(revisions.size() - 1).getEntity();
					
					assertEquals(secondModifier, revisionOrganization.getModifier());
				}
			});
		} finally {
			logout();
		}
	}
	
	@Test
	public void testRevisionDetail() {
		loginAsAdmin(adminModifier);
		try {
			type = constructTestType(this.testTypeName);
			saveInTransaction(type, treeTypeRepository);
			
			node = constructTestNode(null, type);
			for (int index = 0; index < 10; index++) {
				node.setCode(testName + "_" + index);
				node.setName(testName + "_" + index);
				node = saveInTransaction(node, treeNodeRepository);
			}
			getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus arg0) {
					List<Revision<Integer, ? extends BaseEntity>> revisions = auditService.findRevisions(IdmTreeNode.class, node.getId());
					
					assertEquals(10, revisions.size());
					
					for (Revision<Integer, ? extends BaseEntity> rev : revisions) {
						Revision<Integer, ? extends BaseEntity> revSecond = auditService.findRevision(IdmTreeNode.class, rev.getRevisionNumber(), node.getId());
						assertEquals(rev, revSecond);
					}
					
				}
			});
		} finally {
			logout();
		}
	}
	
	@Test
	public void testOrganizationController() {
		loginAsAdmin(adminModifier);
		try {
			getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus arg0) {
					type = constructTestType(testTypeName);
					saveInTransaction(type, treeTypeRepository);
					
					node = constructTestNode(null, type);
					node = saveInTransaction(node, treeNodeRepository);
					
					String nonExistOrganizationId = "" + Integer.MAX_VALUE;
					
					ResponseEntity<ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>> result = treeNodeController.findRevisions(node.getId().toString());
					
					assertEquals(true, result.hasBody());
					
					Exception exception = null;
					
					try {
						treeNodeController.findRevisions(nonExistOrganizationId);
					} catch (ResultCodeException e) {
						exception = e;
					} catch (Exception e) {
						// do nothing
					}
					
					assertNotNull(exception);
					
					exception = null;
					
					try {
						treeNodeController.findRevision(nonExistOrganizationId, Integer.MAX_VALUE);
					} catch (ResultCodeException e) {
						exception = e;
					} catch (Exception e) {
						// do nothing
					}
					
					assertNotNull(exception);
				}
			});
		} finally {
			logout();
		}
	}
	
	private IdmTreeType constructTestType(String name) {
		IdmTreeType type = new IdmTreeType();
		type.setCode(name);
		type.setName(name);
		return type;
	}
	
	private IdmTreeNode constructTestNode(IdmTreeNode parent, IdmTreeType type) {
		IdmTreeNode node = new IdmTreeNode();
		node.setCode(testName);
		node.setName(testName);
		node.setTreeType(type);
		
		if (parent != null) {
			node.setParent(parent);
		}
		
		return node;
	}
}
