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
	
	/**
	 * Maximum updates of entity
	 */
	private static final long MAX_UPDATE = 1000000;
	
	/**
	 * For each {@value PRINT_PROGRESS} print some information about progres
	 * and save average
	 */
	private static final long PRINT_PROGRESS = 100;
	
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
		List<Result> averageList = new ArrayList<Result>();
		
		// create identity
		IdmIdentity identity = this.createTestIdentity();
		
		LOG.info("Start update identity");
		
		long startTime = System.currentTimeMillis();
		
		long startTimeAverage = System.currentTimeMillis();
		
		for (long index = 0; index < MAX_UPDATE; index++) {
			// create one update
			identity = updatetestIdenityComplex(identity, index);
			
			// print some info about progress
			// and set average for PRINT_PROGRESS
			if (index % PRINT_PROGRESS == 0) {
				if (index == 0) {continue;}
				Result resultAverage = new Result(index);
				resultAverage.setDuration(startTimeAverage, System.currentTimeMillis());
				Double percent = (Double.valueOf(index) / Double.valueOf(MAX_UPDATE)) * 100d;
				double average = resultAverage.duration / index;
				LOG.info("Status [{}](%), index [{}] average time for save one entity is [{}]ms" , String.valueOf(percent), index, String.valueOf(average));
				averageList.add(resultAverage);
				
				startTimeAverage = System.currentTimeMillis();
			}
		}
		long finishTime = System.currentTimeMillis();
		
		LOG.info("Finish update identity");
		
		int auditSize = auditService.findRevisions(IdmIdentity.class, identity.getId()).size();
		
		LOG.info("Time of execute [{}]", finishTime - startTime);
		LOG.info("Average time for one entity update [{}]", (finishTime - startTime) / MAX_UPDATE);
		LOG.info("Audit counts [{}]", auditSize);
		
		
		LOG.info("1. record [{}]", averageList.get(0).toString());
		LOG.info("100 000. record [{}]", averageList.get(100).toString());
		LOG.info("200 000. record [{}]", averageList.get(200).toString());
		LOG.info("300 000. record [{}]", averageList.get(300).toString());
		LOG.info("400 000. record [{}]", averageList.get(400).toString());
		LOG.info("500 000. record [{}]", averageList.get(500).toString());
		LOG.info("600 000. record [{}]", averageList.get(600).toString());
		LOG.info("700 000. record [{}]", averageList.get(700).toString());
		LOG.info("800 000. record [{}]", averageList.get(800).toString());
		LOG.info("900 000. record [{}]", averageList.get(900).toString());
		LOG.info("1 000 000. record [{}]", averageList.get(999).toString());
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
		
		@Override
		public String toString() {
			return "ID: " + id + ", duration: " + duration + ", average: " + duration / id;
		}
	}
}


