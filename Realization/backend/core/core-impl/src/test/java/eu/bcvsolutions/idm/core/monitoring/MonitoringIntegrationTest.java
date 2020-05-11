package eu.bcvsolutions.idm.core.monitoring;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringTypeDto;
import eu.bcvsolutions.idm.core.api.service.MonitoringManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Monitoring integration test
 * 
 * @author Vít Švanda
 *
 */
public class MonitoringIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private MonitoringManager monitoringManager;

	@Before
	public void login() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	/**
	 * Green line test for DB count.
	 */
	@Test
	public void testDatabaseCount() {
		IdmMonitoringTypeDto monitoringType = monitoringManager.check(MonitoringManager.MONITORING_TYPE_DATABASE);
		Assert.assertNotNull(monitoringType);
		List<IdmMonitoringResultDto> results = monitoringType.getResults();
		
		long countOfCoreTables = results.stream()
				.filter(result -> "core".equals(result.getModule()))
				.count();
		
		Assert.assertEquals(5, countOfCoreTables);
	}
}
