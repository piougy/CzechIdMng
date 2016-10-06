package eu.bcvsolutions.idm.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

public class OptimisticLockTest extends AbstractIntegrationTest {

	@Autowired
	private IdmIdentityRepository identityRepository;
	
	@PersistenceContext
    private EntityManager entityManager;
	
	@Transactional
	@Test(expected = ObjectOptimisticLockingFailureException.class)
	public void testOptimisticLockException() {		
		IdmIdentity identityOne = identityRepository.findOneByUsername("tomiska");
		entityManager.detach(identityOne);
		IdmIdentity identityTwo = identityRepository.findOneByUsername("tomiska");
		entityManager.detach(identityTwo);
		assertNull(identityOne.getTitleAfter());
		assertNull(identityOne.getTitleBefore());
		assertNull(identityTwo.getTitleAfter());
		assertNull(identityTwo.getTitleBefore());
		
		identityOne.setTitleAfter("after");
		identityTwo.setTitleBefore("before");
		
		assertEquals("after", identityOne.getTitleAfter());
		assertNull(identityOne.getTitleBefore());
		assertNull(identityTwo.getTitleAfter());
		assertEquals("before", identityTwo.getTitleBefore());
		
		saveInNewTransaction(identityOne);
		entityManager.detach(identityOne);
		entityManager.flush();
		
		assertEquals("after", identityOne.getTitleAfter());
		assertNull(identityOne.getTitleBefore());
		assertNull(identityTwo.getTitleAfter());
		assertEquals("before", identityTwo.getTitleBefore());
		
		saveInNewTransaction(identityTwo);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	private void saveInNewTransaction(IdmIdentity identity) {
		identityRepository.save(identity);
	}
	
}
