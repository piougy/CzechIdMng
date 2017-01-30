package eu.bcvsolutions.idm.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuditService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Stress test for audit.
 * Create entity, then update for 1.000.000 times.
 * 
 * All information about stress test will be print into console output
 * 
 * All test will be ignored - to time to finish.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class DefaultAuditStressTest extends AbstractIntegrationTest {
	
	private static final String TEST_IDENTITY = "audit_test_identity";
	
	private static final long MAX_UPDATE = 1000;
	
	private Random r = new Random();
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultAuditStressTest.class);
	
	@Autowired
	private IdmIdentityRepository identityRepository;
	
	@Autowired
	private IdmAuditService auditService;
	
	@Before
	public void login() {
		loginAsAdmin(InitTestData.TEST_ADMIN_USERNAME);
	}
	
	@After 
	public void logout() {
		super.logout();
	}
	
	@Test
	@Ignore
	public void createAndUpdateIdentitySimple() {
		// create identity
		List<Result> results = new ArrayList<Result>();
		IdmIdentity identity = this.createTestIdentity();
		
		long startTime = System.currentTimeMillis();
		for (long index = 0; index < MAX_UPDATE; index++) {
			Result result = new Result(index);
			long start = System.currentTimeMillis();
			identity = updatetestIdenityComplex(identity, index);
			result.setDuration(start, System.currentTimeMillis());
			results.add(result);
		}
		long finishTime = System.currentTimeMillis();
		
		int auditSize = auditService.findRevisions(IdmIdentity.class, identity.getId()).size();
		
		LOG.info("Time of execute [{}]", finishTime - startTime);
		LOG.info("Average time for one entity update [{}]", (finishTime - startTime) / MAX_UPDATE);
		LOG.info("Audit counts [{}]", auditSize);
	}
	
	/**
	 * Create test identity
	 * 
	 * @return
	 */
	private IdmIdentity createTestIdentity() {
		IdmIdentity newIdentity = new IdmIdentity();
		newIdentity.setUsername(TEST_IDENTITY);
		newIdentity.setFirstName(TEST_IDENTITY);
		newIdentity.setLastName(TEST_IDENTITY);
		return this.identityRepository.save(newIdentity);
	}
	
	/**
	 * Method do random update if some attributes of identity.
	 * 
	 * @param identity
	 * @param index
	 * @return
	 */
	private IdmIdentity updatetestIdenityComplex(IdmIdentity identity, long index) {
		if (r.nextBoolean()) {
			identity.setFirstName(TEST_IDENTITY + "_" + index);
		}
		if (r.nextBoolean()) {
			identity.setDisabled(r.nextBoolean());
		}
		if (r.nextBoolean()) {
			identity.setLastName(TEST_IDENTITY + "_" + index);	
		}
		if (r.nextBoolean()) {
			identity.setEmail(index + "test@email.eu");;
		}
		if (r.nextBoolean()) {
			identity.setTitleAfter(TEST_IDENTITY + "_" + index);
		}
		if (r.nextBoolean()) {
			identity.setDescription(TEST_IDENTITY + "_" + index);
		}
		return this.identityRepository.save(identity);
	}

	public class Result {
		long id;
		long duration;
		
		Result(long id) {
			this.id = id;
		}
		
		private void setDuration(long start, long finish) {
			this.duration = finish - start;
		}
	}
}


