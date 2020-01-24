package eu.bcvsolutions.idm.tool.service.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;

/**
 * Test release on mock repository.
 * 
 * @author Radek Tomiška
 *
 */
public class ProductReleaseManagerUnitTest extends AbstractReleaseManagerUnitTest {

	private ProductReleaseManager releaseManager = new ProductReleaseManager();
	
	@Override
	protected AbstractReleaseManager getReleaseManager() {
		return releaseManager;
	}
	
	/**
	 * Prepare mock repository in target folder for test purposes.
	 * 
	 * @return
	 */
	@Override
	protected String prepareRepository() {
		String version = "1.0.0-SNAPSHOT";
		//
		try {
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
			for (String module : ProductReleaseManager.FRONTEND_MODULES) {
				FileUtils.writeStringToFile(new File(frontendFolder.getPath() +"/czechidm-" + module + "/package.json"),
						"{ \"version\" : \"1.0.0-snapshot\" }",
						AttachableEntity.DEFAULT_CHARSET);
			}
			for (String module : ProductReleaseManager.BACKEND_MODULES) {
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
							"</project>",
							AttachableEntity.DEFAULT_CHARSET);
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
							"</project>",
							AttachableEntity.DEFAULT_CHARSET);
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
							"</project>",
							AttachableEntity.DEFAULT_CHARSET);
				}
			}
			//
			releaseManager.setRepositoryRoot(productRootFolder.getPath());
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
