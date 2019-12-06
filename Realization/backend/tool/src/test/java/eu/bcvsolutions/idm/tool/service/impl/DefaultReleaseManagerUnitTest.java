package eu.bcvsolutions.idm.tool.service.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.bcvsolutions.idm.test.api.AbstractUnitTest;
import eu.bcvsolutions.idm.tool.service.api.ReleaseManager;

/**
 * Test release on mock repository.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultReleaseManagerUnitTest extends AbstractUnitTest {

	private DefaultReleaseManager releaseManager = new DefaultReleaseManager();
	
	@BeforeClass
	public static void disableTestsOnDocumentation() {
		// generalize unit test, but it's integration test (MAVEN_HOME) is needed 
	    Boolean documentationOnly = Boolean.valueOf(System.getProperty("documentationOnly", "false"));
	    Assume.assumeFalse(documentationOnly);
	}
	
	@Before
	public void initRepository() {
		Assert.assertEquals("1.0.0-SNAPSHOT", prepareRepository());
	}
	
	@Test
	public void testSetVersion() {
		Assert.assertEquals("1.0.0-SNAPSHOT", releaseManager.getCurrentVersion());
		//
		String newVesion = "2.0.0-SNAPSHOT";
		Assert.assertEquals(newVesion, releaseManager.setVersion(newVesion));
		Assert.assertEquals(newVesion, releaseManager.getCurrentVersion());
		//
		Assert.assertEquals("1.0.0-SNAPSHOT", releaseManager.revertRelease());
		Assert.assertEquals("1.0.0-SNAPSHOT", releaseManager.getCurrentVersion());
	}
	
	@Test
	public void testSetSameVersion() {
		Assert.assertEquals("1.0.0-SNAPSHOT", releaseManager.getCurrentVersion());
		//
		Assert.assertEquals("1.0.0-SNAPSHOT", releaseManager.setVersion("1.0.0-SNAPSHOT"));
	}
	
	@Test
	public void testSetSnapshotVersion() {
		Assert.assertEquals("1.0.0-SNAPSHOT", releaseManager.getCurrentVersion());
		//
		Assert.assertEquals("1.0.1-SNAPSHOT", releaseManager.setSnapshotVersion("1.0.1"));
	}
	
	@Test
	public void testNextSnapshotVersionNumber() {
		Assert.assertEquals("1.0.1-SNAPSHOT", releaseManager.getNextSnapshotVersionNumber("1.0.0-SNAPSHOT"));
		Assert.assertEquals("1.1.24-SNAPSHOT", releaseManager.getNextSnapshotVersionNumber("1.1.23-SNAPSHOT"));
	}
	
	@Test
	public void testNotSemanticNextSnapshotVersionNumber() {
		Assert.assertEquals("mock.1-SNAPSHOT", releaseManager.getNextSnapshotVersionNumber("mock"));
	}
	
	@Test
	public void testCreateSnapshotTag() {
		Assert.assertEquals("1.0.1-SNAPSHOT", releaseManager.gitCreateTag("1.0.1-SNAPSHOT"));
	}
	
	@Test
	public void testRelease() {
		Assert.assertEquals("1.0.0-SNAPSHOT", releaseManager.getCurrentVersion());
		releaseManager.gitAdd();
		releaseManager.gitCommit("before test release");
		//
		Assert.assertEquals("1.0.0", releaseManager.release(null, null));
		releaseManager.gitSwitchBranch("develop");
		Assert.assertEquals("1.0.1-SNAPSHOT", releaseManager.getCurrentVersion());
		releaseManager.gitSwitchBranch("master");
		Assert.assertEquals("1.0.0", releaseManager.getCurrentVersion());
		// just finish UC - local repository without origin cannot be pushed ...
		releaseManager.publishRelease();
	}
	
	@Test
	public void testReleaseDifferentBranches() {
		Assert.assertEquals("1.0.0-SNAPSHOT", releaseManager.getCurrentVersion());
		try {
			String develop = "hotfix";
			String master = "lts-master";
			
			releaseManager.gitCreateBranch("hotfix");
			releaseManager.gitSwitchBranch("hotfix");
			releaseManager.setDevelopBranch(develop);
			releaseManager.setVersion("2.3.6-SNAPSHOT");
			releaseManager.gitAdd();
			releaseManager.gitCommit("start hotfix");
			//
			releaseManager.gitCreateBranch(master);
			releaseManager.setMasterBranch(master);
			//
			Assert.assertEquals("3.5.6", releaseManager.release("3.5.6", "4.0.0-SNAPSHOT"));
			releaseManager.gitSwitchBranch(develop);
			Assert.assertEquals("4.0.0-SNAPSHOT", releaseManager.getCurrentVersion());
			releaseManager.gitSwitchBranch(master);
			Assert.assertEquals("3.5.6", releaseManager.getCurrentVersion());
		} finally {
			releaseManager.setDevelopBranch("develop");
			releaseManager.setMasterBranch("master");
		}
	}
	
	@Test
	public void testReleaseNotMergeMaster() {
		Assert.assertEquals("1.0.0-SNAPSHOT", releaseManager.getCurrentVersion());
		//
		releaseManager.setVersion("1.0.1");
		releaseManager.gitAdd();
		releaseManager.gitCommit("develop branch");
		releaseManager.gitSwitchBranch("master");
		releaseManager.gitMerge("develop");
		releaseManager.gitAdd();
		releaseManager.gitCommit("merge develop branch");
		Assert.assertEquals("1.0.1", releaseManager.getCurrentVersion());
		//
		releaseManager.setVersion("2.0.1-SNAPSHOT");
		releaseManager.gitAdd();
		releaseManager.gitCommit("new develop version");
		try {
			releaseManager.setMasterBranch(null);
			Assert.assertEquals("2.0.1", releaseManager.release(null, null));
			//
			Assert.assertEquals("1.0.1", releaseManager.getCurrentVersion("master"));
			Assert.assertEquals("2.0.2-SNAPSHOT", releaseManager.getCurrentVersion("develop"));
		} finally {
			releaseManager.setMasterBranch("master");
		}

	}
	
	/**
	 * Prepare mock repository in target folder for test purposes.
	 * 
	 * @return
	 */
	private String prepareRepository() {
		String version = "1.0.0-SNAPSHOT";
		
		try {
			releaseManager.setQuiet(true); // don't mess test logs
			releaseManager.setLocal(true); // local test repository only
			//
			// prepare mock repository
			File targetFolder = new File("target");
			Assert.assertTrue(targetFolder.exists());
			File productRootFolder = new File(targetFolder.getPath() + "/mockProductRoot");
			if (productRootFolder.exists()) {
				FileUtils.deleteDirectory(productRootFolder);
			}
			Assert.assertTrue(productRootFolder.mkdir());
			File realizationFolder = new File(productRootFolder.getPath() + "/Realization");
			Assert.assertTrue(realizationFolder.mkdir());
			File backendFolder = new File(realizationFolder.getPath() + "/backend");
			Assert.assertTrue(backendFolder.mkdir());
			File frontendFolder = new File(realizationFolder.getPath() + "/frontend");
			Assert.assertTrue(frontendFolder.mkdir());
			//
			// create all BE and FE modules
			for (String module : ReleaseManager.FRONTEND_MODULES) {
				FileUtils.writeStringToFile(new File(frontendFolder.getPath() +"/czechidm-" + module + "/package.json"),
						"{ \"version\" : \"1.0.0-snapshot\" }");
			}
			for (String module : ReleaseManager.BACKEND_MODULES) {
				if (module.equals("parent")) {
					FileUtils.writeStringToFile(new File(backendFolder.getPath() +"/" + module + "/pom.xml"),
							"<project>"
							+ "<modelVersion>4.0.0</modelVersion>"
							+ "<groupId>eu.bcvsolutions.idm</groupId>"
							+ "<artifactId>idm-" + module + "</artifactId>"
							+ "<packaging>pom</packaging>"
							+ "<version>" + version + "</version>"
							+ "<profiles>"
								+ "<profile>"
									+ "<id>release</id>"
								+ "</profile>"
							+ "</profiles>" +
							"</project>");
				} else if (module.equals("aggregator")) {
					FileUtils.writeStringToFile(new File(backendFolder.getPath() +"/" + module + "/pom.xml"),
							"<project>"
							+ "<modelVersion>4.0.0</modelVersion>"
							+ "<groupId>eu.bcvsolutions.idm</groupId>"
							+ "<artifactId>idm-" + module + "</artifactId>"
							+ "<packaging>pom</packaging>"
							+ "<version>" + version + "</version>"
							+ "<modules>"
								+ "<module>../parent</module>"
								+ "<module>../core</module>"
								+ "<module>../core/core-api</module>"
								+ "<module>../core/core-impl</module>"
								+ "<module>../core/core-test-api</module>"
								+ "<module>../app</module>"
								+ "<module>../example</module>"
								+ "<module>../ic</module>"
								+ "<module>../acc</module>"
								+ "<module>../vs</module>"
								+ "<module>../rpt</module>"
								+ "<module>../rpt/rpt-api</module>"
								+ "<module>../rpt/rpt-impl</module>"
								+ "<module>../tool</module>"
							+ "</modules>"
								+ "<profiles>"
								+ "<profile>"
									+ "<id>release</id>"
								+ "</profile>"
							+ "</profiles>" +	
							"</project>");
				} else {
					FileUtils.writeStringToFile(new File(backendFolder.getPath() +"/" + module + "/pom.xml"),
							"<project>"
							+ "<modelVersion>4.0.0</modelVersion>"
							+ "<parent>"
							  + "<relativePath>../" + (module.contains("/") ? "../" : "") + "parent/pom.xml</relativePath>" 
							  + "<groupId>eu.bcvsolutions.idm</groupId>"
							  + "<artifactId>idm-parent</artifactId>"
							  + "<version>" + version + "</version>"
							+ "</parent>"
							+ "<artifactId>idm-" + module.replaceAll("/", "-") + "</artifactId>"
							+ "<packaging>jar</packaging>" +
							"</project>");
				}
			}
			//
			releaseManager.setProductRoot(productRootFolder.getPath());
			releaseManager.gitInitRepository();
			releaseManager.gitCreateBranch("develop");
			releaseManager.init();
			releaseManager.gitSwitchBranch("develop");
			//
			return version;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
}
