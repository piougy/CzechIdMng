package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import eu.bcvsolutions.idm.core.AbstractIntegrationTest;
import eu.bcvsolutions.idm.core.model.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmOrganization;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmOrganizationRepository;
import eu.bcvsolutions.idm.core.model.service.IdmAuditService;

/**
 * Audit for organization test
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class OrganizationAuditTest extends AbstractIntegrationTest {
	
	@Autowired
	private PlatformTransactionManager platformTransactionManager;
	
	@Autowired
	private IdmOrganizationRepository organizationRepository;
	
	@Autowired
	private IdmAuditService auditService;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	private IdmOrganization organization = null;
	private TransactionTemplate template;
	
	private final String testName = "test_audit_organization";
	private final String systemModifier = "[GUEST]";
	private final String adminModifier = "admin";
	
	@Before
	public void transactionTemplate() {
		template = new TransactionTemplate(platformTransactionManager);
	}
	
	@Test
	public void testCreateRole() {
		organization = saveInTransaction(constructTestOrganization(null), organizationRepository);

		assertNotNull(organization.getId());
		
		template.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus arg0) {
				List<Revision<Integer, ? extends AbstractEntity>> revisions = auditService.findRevisions(IdmOrganization.class, organization.getId());
				
				assertEquals(1, revisions.size());
				
				IdmOrganization org = (IdmOrganization) auditService.findRevision(IdmOrganization.class, 
						revisions.get(revisions.size() - 1).getRevisionNumber(), organization.getId()).getEntity();
				
				assertEquals(organization.getId(), org.getId());
				assertEquals(organization.getName(), org.getName());
				assertEquals(organization.getModifier(), org.getModifier());
			}
		});
	}
	
	@Test
	public void testChangeName() {
		organization = constructTestOrganization(null);
		organization = saveInTransaction(organization, organizationRepository);
		
		organization.setName(testName + "2");
		organization = saveInTransaction(organization, organizationRepository);
		
		template.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus arg0) {
				List<Revision<Integer, ? extends AbstractEntity>> revisions = auditService.findRevisions(IdmOrganization.class, organization.getId());
				assertEquals(2, revisions.size());
				
				IdmOrganization revisionRole = (IdmOrganization) revisions.get(revisions.size() - 1).getEntity();
				
				assertEquals(testName, revisionRole.getName());
				
				revisionRole = (IdmOrganization) revisions.get(revisions.size() - 2).getEntity();
				
				assertEquals(organization.getName(), revisionRole.getName());
			}
		});
	}
	
	@Test
	public void testCheckModifier() {
		organization = constructTestOrganization(null);
		organization.setName(testName + "_2");
		organization = saveInTransaction(organization, organizationRepository);
		
		loginAsAdmin(adminModifier);
		
		organization.setName(testName + "_3");
		organization = saveInTransaction(organization, organizationRepository);
		
		template.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus arg0) {
				Long id = organization.getId();
				
				List<Revision<Integer, ? extends AbstractEntity>> revisions = auditService.findRevisions(IdmOrganization.class, id);
				assertEquals(2, revisions.size());
				
				IdmOrganization revisionOrganization = (IdmOrganization) revisions.get(revisions.size() - 2).getEntity();
				
				assertEquals(systemModifier, revisionOrganization.getModifier());
	
				revisionOrganization = (IdmOrganization) revisions.get(revisions.size() - 1).getEntity();
				
				assertEquals(adminModifier, revisionOrganization.getModifier());
			}
		});
	}
	
	@Test
	public void testRevisionDetail() {
		organization = constructTestOrganization(null);
		for (int index = 0; index < 10; index++) {
			organization.setName(testName + "_" + index);
			organization = saveInTransaction(organization, organizationRepository);
		}
		template.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus arg0) {
				List<Revision<Integer, ? extends AbstractEntity>> revisions = auditService.findRevisions(IdmOrganization.class, organization.getId());
				
				assertEquals(10, revisions.size());
				
				for (Revision<Integer, ? extends AbstractEntity> rev : revisions) {
					Revision<Integer, ? extends AbstractEntity> revSecond = auditService.findRevision(IdmOrganization.class, rev.getRevisionNumber(), organization.getId());
					assertEquals(rev, revSecond);
				}
				
			}
		});
	}
	
	
	private IdmOrganization constructTestOrganization(IdmOrganization parent) {
		IdmOrganization organization = new IdmOrganization();
		organization.setName(testName);
		
		if (parent != null) {
			organization.setParent(parent);
		}
		
		return organization;
	}
	
	private <T extends BaseEntity> T saveInTransaction(final T object, final BaseRepository<T> repository) {
		return template.execute(new TransactionCallback<T>() {
			public T doInTransaction(TransactionStatus transactionStatus) {
				return repository.save(object);
			}
		});
	}
}
