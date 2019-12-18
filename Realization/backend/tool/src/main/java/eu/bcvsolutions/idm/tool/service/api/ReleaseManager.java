package eu.bcvsolutions.idm.tool.service.api;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.tool.exception.ReleaseException;

/**
 * Release manager:
 * - change version
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
	
	void init();
	
	/**
	 * Current version on the active branch. 
	 * All modules have to have the same version, {@link ReleaseException} is thrown otherwise.
	 * 
	 * @param branch name [optional] development branch will be used as default.
	 * @return
	 * @throws ReleaseException if all modules has not the same version.
	 */
	String getCurrentVersion(String branchName);

	/**
	 * Set versions for all modules (frontend and backend too).
	 * Development branch is used (changes are made in development branch only).
	 * 
	 * @param newVersion
	 * @return
	 */
	String setVersion(String newVersion);
	
	/**
	 * Set snapshot  versions for all modules (frontend and backend too).
	 * Development branch is used (changes are made in development branch only).
	 * 
	 * @param newVersion
	 * @return snapshot version
	 */
	String setSnapshotVersion(String newVersion);
	
	/**
	 * Build project under current develop version on develop branch.
	 * Maven 'install' command is used, artifact will be installed into the local maven repository (=> usable for build a module).
	 *  
	 * @return built version
	 */
	String build();
	
	/**
	 * Release project.
	 * - set releaseVersion to development branch ("develop" by default)
	 * - build and deploy project into nexus
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
	 * Revert project files (pom.xml, package.json).
	 * 
	 * @return reverted verion;
	 */
	String revertRelease();	
	
	/**
	 * Path to repository root folder on file system.
	 * 
	 * @return
	 */
	String getRepositoryRoot();
	
	/**
	 * Path to git repository root folder on file system.
	 * 
	 * @param repositoryRoot
	 */
	void setRepositoryRoot(String repositoryRoot);

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
