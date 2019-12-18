package eu.bcvsolutions.idm.tool;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import eu.bcvsolutions.idm.test.api.AbstractUnitTest;
import eu.bcvsolutions.idm.tool.exception.ReleaseException;
import eu.bcvsolutions.idm.tool.service.api.ReleaseManager;

/**
 * Release manager test
 * 
 * @author Radek Tomiška
 *
 */
public class ConsoleRunnerUnitTest extends AbstractUnitTest {
	
	@Mock private ReleaseManager releaseManager;
	//
	@InjectMocks 
	private ConsoleRunner consoleRunner;
	
	@Test
	public void testHelp() {
		consoleRunner.run(new String []{ "-h" });
	}
	
	@Test
	public void testMain() {
		ConsoleRunner.main(new String []{ "-h" });
	}
	
	@Test
	public void testMainModule() {
		ConsoleRunner.main(new String []{ "-h", "-m", "mock" });
	}
	
	@Test
	public void testWrongArgs() {
		consoleRunner.run(new String []{});
	}
	
	@Test(expected = ReleaseException.class)
	public void testException() {
		Mockito.when(releaseManager.getCurrentVersion("branch")).thenThrow(ReleaseException.class);	
		//
		consoleRunner.run(new String []{ "--getVersion", "--developBranch", "branch" });
	}
	
	@Test
	public void testRelease() {
		Mockito.when(releaseManager.release("1", "2")).thenReturn("2");	
		//
		consoleRunner.run(new String []{ "--release", "--releaseVersion", "1", "--developVersion", "2" });
	}
	
	@Test
	public void testBuild() {
		Mockito.when(releaseManager.build()).thenReturn("2");	
		//
		consoleRunner.run(new String []{ "--build" });
	}

	@Test
	public void testPublish() {
		consoleRunner.run(new String []{ "--publish" });
	}
	
	@Test
	public void testSetVersion() {
		Mockito.when(releaseManager.setVersion("2")).thenReturn("2");
		//
		consoleRunner.run(new String []{ "--setVersion", "--developVersion", "2" });
	}
	
	@Test
	public void testGetVersion() {
		Mockito.when(releaseManager.getCurrentVersion("branch")).thenReturn("2");
		//
		consoleRunner.run(new String []{ "--getVersion", "--developBranch", "branch", "--masterBranch", "none" });
	}
	
	@Test
	public void testRevertVersion() {
		consoleRunner.run(new String []{ "--revertVersion" });
	}
	
	@Test
	public void testSetOptionalParameters() {
		consoleRunner.run(new String []{ 
				"--getVersion", 
				"-r", "./mock", 
				"--masterBranch", "mock",
				"--username", "mock",
				"--password", "mock" 
				});
	}
}
