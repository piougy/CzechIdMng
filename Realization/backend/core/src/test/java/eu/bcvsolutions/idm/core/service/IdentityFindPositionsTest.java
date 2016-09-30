package eu.bcvsolutions.idm.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import eu.bcvsolutions.idm.core.AbstractIntegrationTest;
import eu.bcvsolutions.idm.core.model.entity.BaseEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityWorkingPosition;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityWorkingPositionRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityService;

/**
 * Test for identity service find managers and role.
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IdentityFindPositionsTest extends AbstractIntegrationTest{
	
	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	@Autowired
	private IdmIdentityRepository identityRepository;
	
	@Autowired
	private IdmRoleRepository roleRepository;
	
	@Autowired
	private IdmIdentityService identityService;
	
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
	public void findUser() {
		identity = constructTestIdentity();
		identity = saveInTransaction(identity, identityRepository);
		
		IdmIdentity foundIdentity = this.identityService.get(identity.getId());
		
		assertEquals(identity, foundIdentity);
	}
	
	@Test
	@Transactional
	public void findManagers() {
		IdmIdentity user = constructTestIdentity();
		user.setUsername("test_find_managers_user");
		user = saveInTransaction(user, identityRepository);
		
		IdmIdentity manager1 = constructTestIdentity();
		manager1.setUsername("test_find_managers_manager");
		manager1 = saveInTransaction(manager1, identityRepository);
		
		IdmIdentity manager2 = constructTestIdentity();
		manager2.setUsername("test_find_managers_manager2");
		manager2 = saveInTransaction(manager2, identityRepository);
		
		IdmIdentityWorkingPosition position1 = new IdmIdentityWorkingPosition();
		position1.setIdentity(user);
		position1.setPosition("one");
		position1.setManager(manager1);
		saveInTransaction(position1, identityWorkingPositionRepository);
		
		IdmIdentityWorkingPosition position2 = new IdmIdentityWorkingPosition();
		position2.setIdentity(user);
		position2.setPosition("one");
		position2.setManager(manager2);
		saveInTransaction(position2, identityWorkingPositionRepository);
		
		List<IdmIdentity> result = identityService.findAllManagersByUserPositions(user.getId());
		
		assertEquals(2, result.size());

		String resutlString = identityService.findAllManagersByUserPositionsString(user.getId());
		
		assertEquals(true, resutlString.contains(manager1.getUsername()));
		assertEquals(true, resutlString.contains(manager2.getUsername()));
	}
	
	@Test
	public void managerNotFound() {
		IdmIdentity user = constructTestIdentity();
		user.setUsername("user");
		user = saveInTransaction(user, identityRepository);
		
		List<IdmIdentity> result = identityService.findAllManagersByUserPositions(user.getId());
		
		assertEquals(1, result.size());
		
		IdmIdentity admin = result.get(0);
		
		assertNotNull(admin);
	}
	
	@Transactional
	private void deleteAllUser () {
		for	(IdmIdentity user : this.identityRepository.findAll()) {
			identityRepository.delete(user);
		}
	}
	
	private IdmIdentity constructTestIdentity() {
		IdmIdentity identity = new IdmIdentity();
		identity.setUsername("service_test_user");
		identity.setLastName("Service");
		return identity;
	}

	private <T extends BaseEntity> T saveInTransaction(final T object, final BaseRepository<T> repository) {
		return template.execute(new TransactionCallback<T>() {
			public T doInTransaction(TransactionStatus transactionStatus) {
				return repository.save(object);
			}
		});
	}
}
