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
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.AbstractIntegrationTest;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.service.IdmIdentityService;

/**
 * Test for identity service find managers and role.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IdentityFindPositionsTest extends AbstractIntegrationTest{

	@Autowired
	private IdmIdentityRepository identityRepository;
	
	@Autowired
	private IdmIdentityService identityService;
	
	@Autowired
	private IdmIdentityContractRepository identityContractRepository;

	@PersistenceContext
	private EntityManager entityManager;
	
	@Before
	public void init() {
		loginAsAdmin("admin");
	}
	
	@After
	public void deleteIdentity() {
		logout();
	}
	
	@Test
	public void findUser() {
		IdmIdentity identity = constructTestIdentity();
		identity = identityRepository.save(identity);
		
		IdmIdentity foundIdentity = this.identityService.get(identity.getId());
		
		assertEquals(identity, foundIdentity);
	}
	
	@Test
	@Transactional
	public void findGuarantee() {
		IdmIdentity user = constructTestIdentity();
		user.setUsername("test_find_managers_user");
		user = identityRepository.save(user);
		
		IdmIdentity manager1 = constructTestIdentity();
		manager1.setUsername("test_find_managers_manager");
		manager1 = identityRepository.save(manager1);
		
		IdmIdentity manager2 = constructTestIdentity();
		manager2.setUsername("test_find_managers_manager2");
		manager2 = identityRepository.save(manager2);
		
		IdmIdentityContract position1 = new IdmIdentityContract();
		position1.setIdentity(user);
		position1.setGuarantee(manager1);
		identityContractRepository.save(position1);
		
		IdmIdentityContract position2 = new IdmIdentityContract();
		position2.setIdentity(user);
		position2.setGuarantee(manager2);
		identityContractRepository.save(position2);
		
		List<IdmIdentity> result = identityService.findAllManagers(user, null);
		
		assertEquals(2, result.size());

		String resutlString = identityService.findAllManagersAsString(user.getId());
		
		assertEquals(true, resutlString.contains(manager1.getUsername()));
		assertEquals(true, resutlString.contains(manager2.getUsername()));
	}
	
	@Test
	public void managerNotFound() {
		IdmIdentity user = constructTestIdentity();
		user.setUsername("user");
		user = identityRepository.save(user);
		
		List<IdmIdentity> result = identityService.findAllManagers(user, null);
		
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
}
