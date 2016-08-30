package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.DefaultRevisionEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import eu.bcvsolutions.idm.core.AbstractIntegrationTest;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.model.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmOrganization;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmOrganizationRepository;
import eu.bcvsolutions.idm.core.model.service.IdmAuditService;
import eu.bcvsolutions.idm.core.rest.IdmOrganizationController;

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
	private IdmOrganizationController organizationController;
	
	@Autowired
	private IdmAuditService auditService;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	private IdmOrganization organization = null;
	private TransactionTemplate template;
	
	private final String testName = "test_audit_organization";
	private final String adminModifier = "admin";
	
	@Before
	public void transactionTemplate() {
		template = new TransactionTemplate(platformTransactionManager);
	}
	
	@After
	@Transactional
	public void deleteOrganization() {
		// we need to ensure "rollback" manually the same as we are starting transaction manually		
		organizationRepository.delete(organization);
	}
	
	@Test
	public void testCreateRole() {
		loginAsAdmin(adminModifier);
		try {
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
		} finally {
			logout();
		}
	}
	
	@Test
	public void testChangeName() {
		loginAsAdmin(adminModifier);
		try {
			organization = constructTestOrganization(null);
			organization = saveInTransaction(organization, organizationRepository);
			
			final String firstName = organization.getName();
			
			organization.setName(testName + "2");
			organization = saveInTransaction(organization, organizationRepository);
			
			final String secondName = organization.getName();
			
			template.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus arg0) {
					List<Revision<Integer, ? extends AbstractEntity>> revisions = auditService.findRevisions(IdmOrganization.class, organization.getId());
					assertEquals(2, revisions.size());
					
					Collections.sort(revisions, new Comparator<Revision<Integer, ? extends AbstractEntity>>() {
						@Override
						public int compare(Revision<Integer, ? extends AbstractEntity> o1,
								Revision<Integer, ? extends AbstractEntity> o2) {
							return o1.compareTo(o2);
						}
					});
					
					IdmOrganization revisionRole = (IdmOrganization) revisions.get(revisions.size() - 1).getEntity();
					
					assertEquals(secondName, revisionRole.getName());
					
					revisionRole = (IdmOrganization) revisions.get(revisions.size() - 2).getEntity();
					
					assertEquals(firstName, revisionRole.getName());
				}
			});
		} finally {
			logout();
		}
	}
	
	@Test
	public void testCheckModifier() {
		organization = constructTestOrganization(null);
		organization.setName(testName + "_2");
		organization = saveInTransaction(organization, organizationRepository);		
		
		final String firstModifier = organization.getModifier();
		
		loginAsAdmin(adminModifier);
		
		try {
			organization.setName(testName + "_3");
			organization = saveInTransaction(organization, organizationRepository);
			
			final String secondModifier = organization.getModifier();
			
			template.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus arg0) {
					Long id = organization.getId();
					
					List<Revision<Integer, ? extends AbstractEntity>> revisions = auditService.findRevisions(IdmOrganization.class, id);
					assertEquals(2, revisions.size());
					
					Collections.sort(revisions, new Comparator<Revision<Integer, ? extends AbstractEntity>>() {
						@Override
						public int compare(Revision<Integer, ? extends AbstractEntity> o1,
								Revision<Integer, ? extends AbstractEntity> o2) {
							return o1.compareTo(o2);
						}
					});
					
					IdmOrganization revisionOrganization = (IdmOrganization) revisions.get(revisions.size() - 2).getEntity();
					
					assertEquals(firstModifier, revisionOrganization.getModifier());
					
					revisionOrganization = (IdmOrganization) revisions.get(revisions.size() - 1).getEntity();
					
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
		} finally {
			logout();
		}
	}
	
	@Test
	public void testOrganizationController() {
		loginAsAdmin(adminModifier);
		try {
			template.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus arg0) {
					organization = constructTestOrganization(null);
					organization = saveInTransaction(organization, organizationRepository);
					
					String nonExistOrganizationId = "" + Integer.MAX_VALUE;
					
					ResponseEntity<ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>> result = organizationController.findRevisions(organization.getId().toString());
					
					assertEquals(true, result.hasBody());
					
					Exception exception = null;
					
					try {
						organizationController.findRevisions(nonExistOrganizationId);
					} catch (ResultCodeException e) {
						exception = e;
					} catch (Exception e) {
						// do nothing
					}
					
					assertNotNull(exception);
					
					exception = null;
					
					try {
						organizationController.findRevision(nonExistOrganizationId, Integer.MAX_VALUE);
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
