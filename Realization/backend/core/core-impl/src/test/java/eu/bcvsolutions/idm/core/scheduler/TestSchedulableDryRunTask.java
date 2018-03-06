package eu.bcvsolutions.idm.core.scheduler;

/**
 * Test task for {@link DefaultSchedulerManagerIntegrationTest}.
 * 
 * @author Radek Tomiška
 *
 */
public class TestSchedulableDryRunTask extends TestSchedulableTask {
	
	@Override
	public boolean supportsDryRun() {
		return true;
	}
}
