package eu.bcvsolutions.idm.tool;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import eu.bcvsolutions.idm.test.api.AbstractUnitTest;
import eu.bcvsolutions.idm.tool.exception.BuildException;
import eu.bcvsolutions.idm.tool.exception.ReleaseException;
import eu.bcvsolutions.idm.tool.service.impl.ProductReleaseManager;
import eu.bcvsolutions.idm.tool.service.impl.ProjectManager;

/**
 * Console runner test
 * - just formal test to propagate given command line arguments properly.
 * 
 * @author Radek Tomiška
 *
 */
public class ConsoleRunnerUnitTest extends AbstractUnitTest {
	
	@Mock private ProductReleaseManager productReleaseManager;
	@Mock private ProjectManager projectManager;
	//
	@InjectMocks 
	private ConsoleRunner consoleRunner;
	
	@BeforeClass
	public static void disableTestsOnDocumentation() {
		// generalize unit test, but it's integration test (MAVEN_HOME) is needed 
	    Boolean documentationOnly = Boolean.valueOf(System.getProperty("documentationOnly", "false"));
	    Assume.assumeFalse(documentationOnly);
	}
	
	@Test
	public void testHelp() {
		Mockito.when(productReleaseManager.isSnapshotVersion(null)).thenReturn(true);	
		//
		consoleRunner.run(new String []{ "-h" });
	}
	
	@Test
	public void testHelpWithSnapsotLink() {
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
	public void testParseException() {
		Mockito.when(productReleaseManager.getCurrentVersion("branch")).thenThrow(ReleaseException.class);	
		//
		consoleRunner.run(new String []{ "--get-version", "--develop-branch", "branch" });
	}
	
	@Test
	public void testVersion() {
		consoleRunner.run(new String []{ "--version" });
	}
	
	@Test
	public void testRelease() {
		Mockito.when(productReleaseManager.release("1", "2")).thenReturn("2");	
		//
		consoleRunner.run(new String []{ "--release", "--release-version", "1", "--develop-version", "2" });
	}
	
	@Test
	public void testReleasePublish() {
		consoleRunner.run(new String []{ "--release-publish" });
		consoleRunner.run(new String []{ "--release-publish", "--major" });
		consoleRunner.run(new String []{ "--release-publish", "--minor", "--release-version", "1" });
		consoleRunner.run(new String []{ "--release-publish", "--patch" });
		consoleRunner.run(new String []{ "--release-publish", "--hotfix" });
	}
	
	@Test
	public void testBuildProject() {
		consoleRunner.run(new String []{ "--build", "-p" });
	}
	
	@Test(expected = BuildException.class)
	public void testProjectWithoutBuildCommand() {
		consoleRunner.run(new String []{ "-p", "--release" });
	}
	
	@Test
	public void testBuild() {
		Mockito.when(productReleaseManager.build()).thenReturn("2");	
		//
		consoleRunner.run(new String []{ "--build" });
	}

	@Test
	public void testPublish() {
		consoleRunner.run(new String []{ "--publish" });
	}
	
	@Test
	public void testSetVersion() {
		Mockito.when(productReleaseManager.setVersion("2")).thenReturn("2");
		//
		consoleRunner.run(new String []{ "--set-version", "--develop-version", "2" });
	}
	
	@Test
	public void testGetVersion() {
		Mockito.when(productReleaseManager.getCurrentVersion("branch")).thenReturn("2");
		//
		consoleRunner.run(new String []{ "--get-version", "--develop-branch", "branch", "--master-branch", "none" });
	}
	
	@Test
	public void testRevertVersion() {
		consoleRunner.run(new String []{ "--revert-version" });
	}
	
	@Test
	public void testSetOptionalParameters() {
		consoleRunner.run(new String []{
				"--get-version", 
				"-r", "./mock", 
				"--master-branch", "mock",
				"--username", "mock",
				"--password", "mock",
				"--maven-home", "mock",
				"--node-home", "mock",
				"--force",
				"--clean"
				});
	}
}
