package eu.bcvsolutions.idm.tool.service.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;

/**
 * Test release on mock repository.
 * 
 * Single module by module archetype.
 * 
 * @author Radek Tomiška
 *
 */
public class SingleModuleWithoutFrontendReleaseManagerUnitTest extends AbstractReleaseManagerUnitTest {
	
	protected final static String ROOT_MODULE_ID = "module";

	private ModuleReleaseManager releaseManager = new ModuleReleaseManager(ROOT_MODULE_ID);
	 
    @Override
	protected AbstractReleaseManager getReleaseManager() {
		return releaseManager;
	}
	
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
			File repositoryRootFolder = new File(targetFolder.getPath() + "/mockSimpleModuleRoot");
			if (repositoryRootFolder.exists()) {
				FileUtils.deleteDirectory(repositoryRootFolder);
			}
			Assert.assertTrue(repositoryRootFolder.mkdir());
			File realizationFolder = new File(repositoryRootFolder.getPath() + "/Realization");
			Assert.assertTrue(realizationFolder.mkdir());
			File backendFolder = new File(realizationFolder.getPath() + "/backend");
			Assert.assertTrue(backendFolder.mkdir());
			
			FileUtils.writeStringToFile(new File(backendFolder.getPath() +"/" + ModuleReleaseManager.IDM_PREFIX_BACKEND + ROOT_MODULE_ID + "/pom.xml"),
					"<project>"
					+ "<modelVersion>4.0.0</modelVersion>"
					+ "<parent>"
					  + "<groupId>eu.bcvsolutions.idm</groupId>"
					  + "<artifactId>idm-parent</artifactId>"
					  + "<version>10.0.0</version>"
					+ "</parent>"
					+ "<artifactId>idm-" + ROOT_MODULE_ID + "</artifactId>"
					+ "<packaging>jar</packaging>"
					+ "<version>" + version + "</version>" +
					"</project>",
					AttachableEntity.DEFAULT_CHARSET);
			//
			releaseManager.setRepositoryRoot(repositoryRootFolder.getPath());
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
