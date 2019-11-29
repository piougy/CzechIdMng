package eu.bcvsolutions.idm.tool.service.api;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.tool.exception.ReleaseException;

/**
 * Release product:
 * - change product version
 * - prepare release branches and tags
 * - deploy into nexus
 * 
 * @author Radek Tomiška
 *
 */
public interface ReleaseManager {
	
	/**
	 * Development version suffix.
	 */
	String SNAPSHOT_VERSION_SUFFIX = "SNAPSHOT";
	
	/**
	 * Release candidate suffix. (Final resease is without suffix - just semantic version)
	 */
	String RELEASE_CANDIDATE_VERSION_SUFFIX = "RC";

	/**
	 * All product backend modules.
	 */
	String[] BACKEND_MODULES = new String[]{
			"acc",
			"aggregator",
			"app",
			"core",
			"core/core-api",
			"core/core-impl",
			"core/core-test-api",
			"example",
			"ic",
			"parent",
			"rpt",
			"rpt/rpt-api",
			"rpt/rpt-impl",
			"tool", // not supported for 9.7.x versions
			"vs"
	};
	
	/**
	 * All product frontend modules (prefix 'czechidm-' is required by convention).
	 */
	String[] FRONTEND_MODULES = new String[]{
			"acc",
			"app",
			"core",
			"example",
			"rpt",
			"tool", // not supported for 9.7.x versions
			"vs"
	};
	
	void init();
	
	/**
	 * Current product version on the active branch. 
	 * All product modules have to have the same version, {@link ReleaseException} is thrown otherwise.
	 * 
	 * @param branch name [optional] development branch will be used as default.
	 * @return
	 * @throws ReleaseException if all product modules has not the same version.
	 */
	String getCurrentVersion(String branchName);

	/**
	 * Set versions for all product modules (frontend and backend too).
	 * Development branch is used (changes are made in development branch only).
	 * 
	 * @param newVersion
	 * @return
	 */
	String setVersion(String newVersion);
	
	/**
	 * Set snapshot  versions for all product modules (frontend and backend too).
	 * Development branch is used (changes are made in development branch only).
	 * 
	 * @param newVersion
	 * @return
	 */
	String setSnapshotVersion(String newVersion);
	
	/**
	 * Release product.
	 * - set releaseVersion to development branch ("develop" by default)
	 * - build and deploy product into nexus
	 * - create tag with releaseVersion
	 * - merge releaseVersion into production branch ("master" by default) - if production branch is not defined then 
	 *   no merge is executed - sometimes merge is not needed (e.g. when some old hotfix branch is released - set null)
	 * - set newDevelopVersion to development branch ("develop" by default)
	 * 
	 * Release prepares local branches only. Push branches into origin manually or execute {@link #publishRelease()}.
	 * 
	 * @param releaseVersion [optional] Stable semantic version will be used as default (=> current no snapshot version).
	 * @param newDevelopVersion [optional] Next snapshot semantic version will be used as default (=> current snapshot version + 1).
	 * @throws ReleaseException if local changes are present.
	 * @return released version
	 */
	String release(String releaseVersion, String newDevelopVersion);
	
	/**
	 * Push prepared development, production and tags into origin repository.
	 */
	void publishRelease();	
	
	/**
	 * Revert product project files (pom.xml, package.json).
	 * 
	 * @return reverted verion;
	 */
	String revertRelease();	
	
	/**
	 * Path to product root folder on file system.
	 * 
	 * @return
	 */
	String getProductRoot();
	
	/**
	 * Path to product root folder on file system.
	 * 
	 * @param productRoot
	 */
	void setProductRoot(String productRoot);

	/**
	 * Maven home directory (where executable mvn command is placed).
	 * Default - system MAVEN_HOME property is used.
	 *
	 * @param mavenHome
	 */
	void setMavenHome(String mavenHome);

	/**
	 * Branch with feature - working branch.
	 * Default: develop
	 *
	 * @param developBranch
	 */
	void setDevelopBranch(String developBranch);

	/**
	 * Branch with feature - working branch.
	 * Default: develop
	 *
	 * @return
	 */
	String getDevelopBranch();

	/**
	 * Branch for releases - where feature has to be merged after release.
	 * Default: master
	 *
	 * @param productionBranch
	 */
	void setMasterBranch(String masterBranch);

	/**
	 * Branch for releases - where feature has to be merged after release.
	 * Default: master
	 *
	 * @return
	 */
	String getMasterBranch();
	
	/**
	 * Git username for publish release.
	 * 
	 * @param username
	 */
	void setUsername(String username);
	
	/**
	 * Git password for publish release or ssh passphrase.
	 * 
	 * @param password
	 */
	void setPassword(GuardedString password);
}
