package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import eu.bcvsolutions.idm.core.AbstractIntegrationTest;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityWorkingPosition;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityWorkingPositionRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.rest.impl.IdmIdentityController;

/**
 * Test audit configuration on identity entity
 * 
 * @author Radek Tomiška 
 *
 */
public class IdentityAuditTest extends AbstractIntegrationTest {

	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	@Autowired
	private IdmIdentityRepository identityRepository;
	
	@Autowired
	private IdmRoleRepository roleRepository;
	
	@Autowired
	private IdmIdentityRoleRepository identityRoleRepository;
	
	@Autowired
	private IdmIdentityController identityController;	
	
	@Autowired
	private IdmIdentityWorkingPositionRepository identityWorkingPositionRepository;

	@PersistenceContext
	private EntityManager entityManager;
	
	private TransactionTemplate template;
	private IdmIdentity identity;
	private IdmRole role = null;

	@Before
	public void transactionTemplate() {
		template = new TransactionTemplate(platformTransactionManager);
		identity = constructTestIdentity();
		loginAsAdmin("admin");
	}
	
	@After
	@Transactional
	public void deleteIdentity() {
		// we need to ensure "rollback" manually the same as we are starting transaction manually		
		identityRepository.delete(identity);
		if (role != null) {
			roleRepository.delete(role);
			role = null;
		}
		logout();
	}

	@Test
	public void testCreateIdentity() {
		identity = saveInTransaction(identity, identityRepository);

		assertNotNull(identity.getId());

		template.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
				AuditReader reader = AuditReaderFactory.get(entityManager);
				assertEquals(1, reader.getRevisions(IdmIdentity.class, identity.getId()).size());
			}
		});	
	}
	
	@Test
	public void testUpdateIdentity() {
		identity = saveInTransaction(identity, identityRepository);
		identity.setFirstName("One"); 
		identity = saveInTransaction(identity, identityRepository);
		
		template.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				AuditReader reader = AuditReaderFactory.get(entityManager);
				assertEquals(2, reader.getRevisions(IdmIdentity.class, identity.getId()).size());
				
			}
		});			
	}	

	@Test
	public void testWorkingPositionChange() {
		identity = saveInTransaction(identity, identityRepository);
		
		IdmIdentityWorkingPosition position = new IdmIdentityWorkingPosition();
		position.setIdentity(identity);
		position.setPosition("one");
		
		saveInTransaction(position, identityWorkingPositionRepository);
		
		template.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				AuditReader reader = AuditReaderFactory.get(entityManager);
				assertEquals(2, reader.getRevisions(IdmIdentity.class, identity.getId()).size());
				
			}
		});
	}

	@Test
	public void testAssignedRoleChanges() {
		identity = saveInTransaction(identity, identityRepository);
		
		role = new IdmRole();
		role.setName("audit_role");
		
		role = saveInTransaction(role, roleRepository);
		
		IdmIdentityRole identityRole = new IdmIdentityRole();
		identityRole.setIdentity(identity);
		identityRole.setRole(role);
		
		saveInTransaction(identityRole, identityRoleRepository);
		
		template.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				AuditReader reader = AuditReaderFactory.get(entityManager);
				assertEquals(2, reader.getRevisions(IdmIdentity.class, identity.getId()).size());
				
			}
		});
	}
	
	@Test
	public void testIdentityController() {
		template.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus arg0) {
				identity = constructTestIdentity();
				identity = saveInTransaction(identity, identityRepository);
				
				String nonExistIdentityId = "NON_EXIST_IDENTITY_ID";
				
				ResponseEntity<ResourcesWrapper<ResourceWrapper<DefaultRevisionEntity>>> result = identityController.findRevisions(identity.getName());
				
				assertEquals(true, result.hasBody());
				
				Exception exception = null;
				
				try {
					identityController.findRevisions(nonExistIdentityId);
				} catch (ResultCodeException e) {
					exception = e;
				} catch (Exception e) {
					// do nothing
				}
				
				assertNotNull(exception);
				
				exception = null;
				
				try {
					identityController.findRevision(nonExistIdentityId, Integer.MAX_VALUE);
				} catch (ResultCodeException e) {
					exception = e;
				} catch (Exception e) {
					// do nothing
				}
				
				assertNotNull(exception);
			}
		});
	}
	
	private IdmIdentity constructTestIdentity() {
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername("audit_test_user");
		identity.setLastName("Auditor");
		return identity;
	}

	private <T extends BaseEntity> T saveInTransaction(final T object, final BaseRepository<T, ?> repository) {
		return template.execute(new TransactionCallback<T>() {
			public T doInTransaction(TransactionStatus transactionStatus) {
				return repository.save(object);
			}
		});
	}

}
