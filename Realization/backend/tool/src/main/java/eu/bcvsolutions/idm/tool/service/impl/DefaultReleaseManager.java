package eu.bcvsolutions.idm.tool.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.jcraft.jsch.Session;

import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.tool.exception.ReleaseException;
import eu.bcvsolutions.idm.tool.service.api.ReleaseManager;

/**
 * Release product by maven, gulp and git:
 * - change product version - all product modules have to have the same version number.
 * - prepare releasegit branches and tags
 * - deploy into nexus
 * - https://stackoverflow.com/questions/20496084/git-status-ignore-line-endings-identical-files-windows-linux-environment
 *
 * Manager is not thread safe, construct new manager before every usage.
 *
 * @author Radek Tomiška
 *
 */
public class DefaultReleaseManager implements ReleaseManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultReleaseManager.class);
	public static String DEFAULT_PRODUCT_LOCATION = "./CzechIdMng";
	//
	private ObjectMapper mapper;
	// props
	private String productRoot = DEFAULT_PRODUCT_LOCATION; // default location ./CzechIdMng will be used - git clone in the same folder
	private String mavenHome; // MAVEN_HOME will be used as default
	private String developBranch = "develop";
	private String masterBranch = "master";
	private boolean quiet = false; // show log, if process output is not redirected into file.
	private boolean local = false; // local git repository and build only
	private String username; // git username (for publish release)
	private GuardedString password; // git password (for publish realese) or ssh passphrase
	// cache
	private String mavenBaseCommand;
	private Git git;
	
	/**
	 * Release manager with default product location.
	 */
	public DefaultReleaseManager() {
		this(null);
	}

	public DefaultReleaseManager(String productRoot) {
		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		//
		if (!StringUtils.isEmpty(productRoot)) {
			this.productRoot = productRoot;
			//
			LOG.trace("Product location [{}] given.", this.productRoot);
		} else {
			// default location - git clone in the same folder
			this.productRoot = DEFAULT_PRODUCT_LOCATION;
			//
			LOG.trace("Default product location [{}] will be used.", this.productRoot);
		}
	}

	@Override
	@PostConstruct
	public void init() {
		String mavenBaseCommand = getMavenBaseCommand();
		LOG.info("Maven command [{}].", mavenBaseCommand);
		Assert.isTrue(new File(mavenBaseCommand).exists(), String.format("Maven command [%s] not found.", mavenBaseCommand));
		getMavenVersion();
		//
		String productRoot = getProductRoot();
		LOG.info("Product location: [{}].", productRoot);
		Assert.hasLength(productRoot, "Product root is required.");
		Assert.isTrue(new File(productRoot).exists(), String.format("Product root [%s] not found.", productRoot));
		//
		LOG.info("Develop branch: [{}].", getDevelopBranch());
		LOG.info("Master branch: [{}].", getMasterBranch());
		//
		try {
			git = Git.open(new File(productRoot));
		} catch (Exception ex) {
			throw new ReleaseException(String.format("Git repository in product folder [%s] cannot be inited.", productRoot) , ex);
		}
	}

	@Override
	public String getCurrentVersion(String branchName) {
		gitSwitchBranch(StringUtils.isEmpty(branchName) ? getDevelopBranch() : branchName);
		//
		return getCurrentVersion();
	}

	@Override
	public String setVersion(String newVersion) {
		Assert.hasLength(newVersion, "New version is required.");
		// switch to devel version just for sure
		gitSwitchBranch(getDevelopBranch());
		// check all versions are set
		String currentVersion = getCurrentVersion();
		//
		if (currentVersion.equalsIgnoreCase(newVersion)) {
			LOG.warn("Current version is the same as the given new version [{}], nothing to do.", newVersion);
			//
			return currentVersion;
		}
		//
		setBackendVersion(newVersion);
		setFrontendVersion(newVersion);
		//
		// check all versions are set
		return getCurrentVersion();
	}

	@Override
	public String setSnapshotVersion(String newVersion) {
		Assert.hasLength(newVersion, "New version is required.");
		//
		if (!newVersion.endsWith(SNAPSHOT_VERSION_SUFFIX)) {
			newVersion = String.format("%s-%s", newVersion, SNAPSHOT_VERSION_SUFFIX);
		}
		return setVersion(newVersion);
	}

	@Override
	public String release(String releaseVersion, String newDevelopVersion) {
		// no local changes
		if (!gitIsClean()) {
			throw new ReleaseException("Local changes found - push or revert changes before release");
		}
		// switch develop
		gitSwitchBranch(getDevelopBranch());
		gitPull();
		//
		if (StringUtils.isEmpty(releaseVersion)) {
			releaseVersion = getVersionNumber(getCurrentVersion());
		}
		//
		LOG.info("Release product version [{}] ...", releaseVersion);
		//
		// set stable version
		setVersion(releaseVersion);
		gitAdd();
		gitCommit(String.format("Release version [%s] - prepare", releaseVersion));
		// deploy - its saver than merge stable into master (this takes long time and conflict can occurs)
		deploy();
		// package-lock is changed after build + deploy => we need to commit it into tag / master
		gitAdd();
		gitCommit(String.format("Release version [%s] - alfter build", releaseVersion));
		// create tag
		gitCreateTag(releaseVersion);
		// merge into master, if branch is given
		// sometimes merge is not needed (e.g. when some old hotfix branch is released - set null)
		String masterBranch = getMasterBranch();
		if (StringUtils.isNotEmpty(masterBranch)) {
			gitSwitchBranch(masterBranch);
			gitPull();
			// merge develop into master - conflict can occurs => just this and next step schould be repeated.
			gitMerge(getDevelopBranch());
			// switch develop
			gitSwitchBranch(getDevelopBranch());
		}
		// set new develop version version
		if (StringUtils.isEmpty(newDevelopVersion)) {
			newDevelopVersion = getNextSnapshotVersionNumber(releaseVersion);
		}
		setVersion(newDevelopVersion);
		gitAdd();
		gitCommit(String.format("New develop version [%s]", newDevelopVersion));
		//
		LOG.info("Product version released [{}]. New development version [{}].", releaseVersion, newDevelopVersion);
		LOG.info("Branches [{}], [{}] and tag [{}] are prepared to push into origin.",
				getDevelopBranch(), getMasterBranch(), releaseVersion);
		//
		return releaseVersion;
	}

	@Override
	public void publishRelease() {
		String masterBranch = getMasterBranch();
		if (StringUtils.isNotEmpty(masterBranch)) {
			gitPush(masterBranch);
		}
		gitPush(getDevelopBranch());
		gitPushTags();
		//
		LOG.info("Branches [{}], [{}] and prepared tags pushed into origin.",
				getDevelopBranch(), getMasterBranch());
	}

	/**
	 * Output will be shown only on exception.
	 *
	 * @param quiet
	 */
	public void setQuiet(boolean quiet) {
		this.quiet = quiet;
	}
	
	/**
	 * Work with local git repository only and build localy only
	 * 
	 * @param local
	 */
	public void setLocal(boolean local) {
		this.local = local;
	}
	
	@Override
	public String getProductRoot() {
		if (StringUtils.isEmpty(productRoot)) {
			productRoot = DEFAULT_PRODUCT_LOCATION;
		}
		return productRoot;
	}
	
	@Override
	public void setProductRoot(String productRoot) {
		this.productRoot = productRoot;
	}

	@Override
	public void setMavenHome(String mavenHome) {
		this.mavenHome = mavenHome;
	}

	@Override
	public void setDevelopBranch(String developBranch) {
		this.developBranch = developBranch;
	}

	@Override
	public String getDevelopBranch() {
		return developBranch;
	}

	@Override
	public void setMasterBranch(String masterBranch) {
		this.masterBranch = masterBranch;
	}

	@Override
	public String getMasterBranch() {
		return masterBranch;
	}
	
	@Override
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Override
	public void setPassword(GuardedString password) {
		this.password = password;
	}

	protected String getCurrentVersion() {
		String currentVersion = null;
		// BE
		for (String backendModule : getBackendModules(getCurrentBackendModuleVersion("parent"))) {

			String moduleVersion = getCurrentBackendModuleVersion(backendModule);
			LOG.debug("Backend module [{}] has current version [{}].", backendModule, moduleVersion);
			if (StringUtils.isEmpty(moduleVersion)) {
				throw new ReleaseException(String.format("Backend module [%s] version cannot be resolved.", backendModule));
			}
			if (currentVersion == null) {
				currentVersion = moduleVersion;
			} else if(!currentVersion.equals(moduleVersion)) {
				throw new ReleaseException(
						String.format("Backend module [%s] has wrong version [%s], expected version [%s].",
								backendModule, moduleVersion, currentVersion));
			}
		}
		// FE
		for (String frontendModule : getFrontendModules(currentVersion)) {
			String moduleVersion = getCurrentFrontendModuleVersion(frontendModule);
			LOG.debug("Frontend module [{}] has current version [{}].", frontendModule, moduleVersion);
			if (StringUtils.isEmpty(moduleVersion)) {
				throw new ReleaseException(String.format("Frontend module [%s] version cannot be resolved.", moduleVersion));
			}
			if (currentVersion == null) {
				currentVersion = moduleVersion;
			} else if(!currentVersion.equalsIgnoreCase(moduleVersion)) {
				throw new ReleaseException(
						String.format("Frontend module [%s] has wrong version [%s], expected version [%s].",
								frontendModule, moduleVersion, currentVersion.toLowerCase()));
			}
		}
		//
		LOG.info("Current product version [{}].", currentVersion);
		//
		return currentVersion;
	}

	protected String getMavenVersion() {
		ProcessBuilder processBuilder = new ProcessBuilder();
    	processBuilder.command(getMavenBaseCommand(), "-v");
    	execute(processBuilder, "Check maven version failed");
    	// TODO: return maven version
    	return null;
	}

	protected void deploy() {
		if (local) {
			LOG.debug("Local repository is used - build only, deployment is not available.");
		}
		//
		String currentVersion = getCurrentVersion();
		//
		// delete symlinks from FE folder
		for (String frontendModule : getFrontendModules(currentVersion)) {
			if (frontendModule.equals("app")) {
				// entrypoint module
				continue;
			}
			File symlink = new File(String.format("%s/node_modules/czechidm-%s", getFrontendModuleBasePath("app"), frontendModule));
			if (symlink.exists()) {
				symlink.delete();
				LOG.debug("Symlink for module [{}] deleted from czechidm-app node-modules folder.", frontendModule);
			} else {
				LOG.debug("Symlink for module [{}] not exists in czechidm-app node-modules folder [path: {}].",
						frontendModule, symlink.getPath());
			}
		}
		LOG.info("Build and deploy product version [{}] by aggregator into nexus, this will take several minutes ...", currentVersion);
		//
		String aggregatorPath = getBackendModuleBasePath("aggregator");
    	ProcessBuilder processBuilder = new ProcessBuilder();
    	processBuilder.command(
    			getMavenBaseCommand(), 
    			"clean", 
    			local ? "install" : "deploy", 
    			"-Prelease", 
    			"-DdocumentationOnly=true");
    	processBuilder.directory(new File(aggregatorPath));
    	execute(processBuilder, "Deploy failed");
    	//
    	LOG.info("Product version [{}] successfully deployed to nexus.", currentVersion);
	}

	protected void setBackendVersion(String fullVersionName) {
		String newVersion = fullVersionName.toUpperCase();
		LOG.info("Setting backend version [{}] ... ", newVersion);
		//
		String aggregatorPath = getBackendModuleBasePath("aggregator");

		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(
				getMavenBaseCommand(),
				"versions:update-parent",
				"-DparentVersion="+newVersion,
				"-DgenerateBackupPoms=false");
		processBuilder.directory(new File(aggregatorPath));
		execute(processBuilder, "Update parent version failed");
		//
		processBuilder = new ProcessBuilder();
		processBuilder.command(
				getMavenBaseCommand(),
				"versions:set",
				"-DnewVersion="+newVersion,
				"-DprocessAllModules=true",
				"-DprocessParent=false",
				"-DgenerateBackupPoms=false");
		processBuilder.directory(new File(aggregatorPath));
		execute(processBuilder, "Set backend versions failed");
		//
		processBuilder = new ProcessBuilder();
		processBuilder.command(
				getMavenBaseCommand(),
				"-N",
				"versions:update-child-modules",
				"-DgenerateBackupPoms=false");
		processBuilder.directory(new File(aggregatorPath));
		execute(processBuilder, "Set backend versions failed");
		execute(processBuilder, "Set backend versions failed"); // FIXME: has to be executed twice ... why ...?
		//
		LOG.info("Backend version set to [{}]", newVersion);
	}

	protected void setFrontendVersion(String fullVersionName) {
		String newVersion = fullVersionName.toLowerCase();
		//
		for (String frontendModule : getFrontendModules(newVersion)) {
			File modulePackage = new File(String.format("%s/package.json", getFrontendModuleBasePath(frontendModule)));
			Assert.isTrue(modulePackage.exists(), String.format("Frontend module [%s] not found on filesystem.", frontendModule));
			//
			try {
				ObjectNode json = (ObjectNode) getPackageJson(modulePackage);
				json.put("version", newVersion);
				//
				try (FileOutputStream outputStream = new FileOutputStream(modulePackage)) {
					JsonGenerator jGenerator = mapper.getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
					mapper.writerWithDefaultPrettyPrinter().writeValue(jGenerator, json);
					jGenerator.close();
					//
					LOG.info("Frontend module [{}] version set to [{}]", frontendModule, newVersion);
				} catch (Exception ex) {
		            throw new ReleaseException(ex);
				}
			} catch (Exception ex) {
	            throw new ReleaseException(ex);
			}
		}
	}

	protected boolean gitIsClean() {
		try {
			Status call = git.status().call();
			return call.isClean();
		} catch (NoWorkTreeException | GitAPIException ex) {
			throw new ReleaseException("Check status failed", ex);
		}
	}

	@Override
	public String revertRelease() {
		gitSwitchBranch(getDevelopBranch());
		//
		String forVersion = getCurrentBackendModuleVersion("parent");
		// checkout doesn't support wildcards, don't know why ...
		List<String> allProjectFiles = Stream
				.concat(
					getBackendModules(forVersion)
						.stream()
						.map(moduleName -> {
							return String.format("%s/pom.xml", getBackendModuleRelativePath(moduleName));
						}),
					getFrontendModules(forVersion)
						.stream()
						.map(moduleName -> {
							return String.format("%s/package.json", getFrontendModuleRelativePath(moduleName));
						})
				)
				.collect(Collectors.toList());
		//
		try {
			git
				.checkout()
				//.setName(getDevelopBranch())
				.addPaths(allProjectFiles)
				.call();
			//
			String currentVersion = getCurrentVersion();
			LOG.info("Product project files (pom.xml, package.json) reverted. Current product version [{}].", currentVersion);
			return currentVersion;
		} catch (Exception ex) {
			throw new ReleaseException("Revert product project files failed", ex);
		}
	}

	protected void gitPull() {
		if (local) {
			LOG.debug("Local repository is used, pull command is not available.");
			return;
		}
		try {
			git
				.pull()
				.setTransportConfigCallback(getTransportConfigCallback())
				.call();
		} catch (Exception ex) {
			throw new ReleaseException("Pull failed", ex);
		}
	}

	public String gitSwitchBranch(String targetBranch) {
		try {
			if (git.getRepository().findRef(targetBranch) == null) {
				// remote branch is not present locally
				git
					.checkout()
			        .setCreateBranch(true)
			        .setName(targetBranch)
			        .setUpstreamMode(SetupUpstreamMode.TRACK)
			        .setStartPoint("origin/" + targetBranch)
			        .call();
			} else {
				git
					.checkout()
					.setName(targetBranch)
					.setUpstreamMode(SetupUpstreamMode.TRACK)
					.call();
			}
			//
			LOG.info("Switched to branch [{}].", targetBranch);
			//
			return targetBranch;
		} catch (Exception ex) {
			throw new ReleaseException("Switch branch failed", ex);
		}
	}

	protected void gitAdd() {
		try {
			git
				.add()
				.addFilepattern(".")
				.call();
		} catch (Exception ex) {
			throw new ReleaseException("Add failed", ex);
		}
	}

	protected void gitCommit(String message) {
		Assert.hasLength(message, "Commit message is required.");
		//
		try {
			git
				.commit()
				.setMessage(message)
				.call();
		} catch (Exception ex) {
			throw new ReleaseException("Commit failed", ex);
		}
	}

	protected String gitCreateTag(String tagVersion) {
		Assert.hasLength(tagVersion, "Tag version (~name) is required.");
		//
		boolean isSnapshotVersion = false;
		if (tagVersion.toUpperCase().endsWith(SNAPSHOT_VERSION_SUFFIX)) {
			LOG.warn("Tag will be created for development version [{}] - it's only for development puprose (should not be pushed to origin).",
					tagVersion);
			isSnapshotVersion = true;
		}

		String message;
		if (isSnapshotVersion) {
			ZonedDateTime now = ZonedDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ConfigurationService.DEFAULT_APP_DATETIME_WITH_SECONDS_FORMAT);
			message = String.format("Snapshot version %s (%s)", tagVersion, formatter.format(now));
		} else {
			message = String.format("Release version %s", tagVersion);
		}
		//
		try {
			git
				.tag()
				.setName(tagVersion)
				.setMessage(message)
				.call();
		} catch (Exception ex) {
			throw new ReleaseException("Tag failed", ex);
		}
    	//
    	if (isSnapshotVersion) {
    		LOG.info("Snapshot tag [{}] created.", tagVersion);
    	} else {
    		LOG.info("Tag [{}] created. Don't forget to push tag to origin if needed.", tagVersion);
    	}
    	//
    	return tagVersion;
	}

	protected void gitMerge(String mergeBranch) {
		try {
			MergeResult res = git
				.merge()
				.include(git.getRepository().findRef(mergeBranch))
				.call();
			if (!res.getMergeStatus().isSuccessful()) {
				throw new ReleaseException(String.format("Merge failed [%s].", res.getMergeStatus()));
			}
		} catch (Exception ex) {
			throw new ReleaseException("Merge failed", ex);
		}
	}

	protected void gitPush(String branch) {
		if (local) {
			LOG.debug("Local repository is used, pull command is not available.");
			return;
		}
		if (StringUtils.isEmpty(branch)) {
			LOG.warn("Given branch is empty, nothing to push.");
			return;
		}
		try {
			git
				.push()
				.add(branch)
				.setTransportConfigCallback(getTransportConfigCallback())
				.call();
		} catch (Exception ex) {
			throw new ReleaseException("Push failed", ex);
		}
	}
	
	protected void gitPushTags() {
		if (local) {
			LOG.debug("Local repository is used, pull command is not available.");
			return;
		}
		try {
			git
				.push()
				.setPushTags()
				.setTransportConfigCallback(getTransportConfigCallback())
				.call();
		} catch (Exception ex) {
			throw new ReleaseException("Push failed", ex);
		}
	}
	
	/**
	 * Init repository - usable for tests
	 */
	@SuppressWarnings("deprecation")
	protected void gitInitRepository() {
		File productRoot = new File(getProductRoot());
		Assert.isTrue(productRoot.exists(), "Product root has to exist.");
		//
		try {
			git = Git
				.init()
				.setDirectory(productRoot)
				.call();
			FileUtils.writeStringToFile(new File(productRoot.getPath() + "/README.md"), "test repo");
			gitAdd();
			gitCommit("start commit");
		} catch (Exception ex) {
			throw new ReleaseException("Init repository failed", ex);
		}
	}
	
	protected String gitCreateBranch(String branchName) {
		try {
			Ref call = git
	    	.branchCreate()
	    	.setName(branchName)
	    	.call();
			//
			return call.getName();
		} catch (Exception ex) {
			throw new ReleaseException("Creating branch failed", ex);
		}
	}

	protected String getVersionNumber(String fullVersionName) {
		return fullVersionName.toUpperCase()
				.replaceFirst("-" + SNAPSHOT_VERSION_SUFFIX, "")
				.replaceFirst("-" + RELEASE_CANDIDATE_VERSION_SUFFIX, "");
	}

	protected String getNextSnapshotVersionNumber(String versionNumber) {
		String versionNumberWithoutSnapshot = getVersionNumber(versionNumber);
		//
		String[] versionNumbers = versionNumberWithoutSnapshot.split("\\.");
		String newVersion;
		try {
			String lastNumberAsString = versionNumbers[versionNumbers.length - 1];
			Integer lastNumber = Integer.valueOf(lastNumberAsString);
			versionNumbers[versionNumbers.length - 1] = String.valueOf(lastNumber + 1);
			//
			newVersion = StringUtils.join(versionNumbers, '.');
		} catch (NumberFormatException ex) {
			// simply append next version number as fallback
			newVersion = String.format("%s.1", versionNumber);
			//
			LOG.warn("Sematic version used as convention for CzechIdM are not used => returning next version with additional position [{}].",
					newVersion);
		}
		//
		return String.format("%s-%s", newVersion, SNAPSHOT_VERSION_SUFFIX);
	}

	/**
	 * Resolved executable mvn command (by given home directory and OS).
	 *
	 * @return
	 */
	protected String getMavenBaseCommand() {
		if (mavenBaseCommand == null) {
			if (StringUtils.isEmpty(mavenHome)) {
				mavenHome = System.getenv("MAVEN_HOME");
			}
			String commandName = "mvn";
			if (StringUtils.isEmpty(mavenHome)) {
				// maven home is not specified, try to execute global command
				mavenBaseCommand = commandName;
			} else {
				String baseCommand = String.format("%s/bin/%s", mavenHome, commandName);
				if (isWindows()) {
					// append cmd for windows
					mavenBaseCommand = String.format("%s.cmd", baseCommand);
				} else {
					mavenBaseCommand = baseCommand;
				}
			}
		}
		//
		return mavenBaseCommand;
	}

	protected String getCurrentFrontendModuleVersion(String moduleName) {
		File modulePackage = new File(String.format("%s/package.json", getFrontendModuleBasePath(moduleName)));
		Assert.isTrue(modulePackage.exists(), String.format("Frontend module [%s] not found on filesystem.", moduleName));
		//
		try {
			JsonNode json = getPackageJson(modulePackage);
			String currentVersion = json.get("version").textValue();
			//
			return currentVersion;
		} catch (Exception ex) {
            throw new ReleaseException(ex);
        }
	}

	protected String getCurrentBackendModuleVersion(String moduleName) {
		File modulePackage = new File(String.format("%s/pom.xml", getBackendModuleBasePath(moduleName)));
		Assert.isTrue(modulePackage.exists(), String.format("Backend module [%s] not found on filesystem.", moduleName));
		//
		try {
			XmlMapper xmlMapper = new XmlMapper();
			JsonNode json = xmlMapper.readTree(modulePackage);
			//
			String currentVersion = null;
			JsonNode versionNode = json.get("version");
			if (versionNode == null) {
				JsonNode parentNode = json.get("parent");
				if (parentNode != null) {
					versionNode = parentNode.get("version");
				}
			}
			if (versionNode != null) {
				currentVersion = versionNode.textValue();
			}
			//
			return currentVersion;
		} catch (Exception ex) {
            throw new ReleaseException(ex);
		}
	}

	/**
	 * Execute process.
	 *
	 * @param processBuilder
	 * @param exceptionMessage
	 * @throws InterruptedException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @return log file with process execution output
	 */
	protected String execute(ProcessBuilder processBuilder, String exceptionMessage) {
		File logFile = null;
		try {
			logFile = File.createTempFile(UUID.randomUUID().toString(), "-log.txt");
			//
			processBuilder.redirectErrorStream(true);
			if (quiet) {
				processBuilder.redirectOutput(logFile);
			} else {
				processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			}
	    	//
	    	Process process = processBuilder.start();
			// wait for end
			process.waitFor();
			// throw exception on failure
			String output = IOUtils.toString(new FileInputStream(logFile), AttachableEntity.DEFAULT_CHARSET);
	    	if (process.exitValue() != 0) {
	    		if (quiet) {
	    			// show log, if process output is not redirected into file.
	    			LOG.error("{}: {}", exceptionMessage, output);
	    		}
	    		throw new ReleaseException(String.format("Update parent version failed [exit code: %s].", process.exitValue()));
	    	}
			//
	    	return output;
		} catch (InterruptedException | IOException ex) {
			throw new ReleaseException(ex);
        } finally {
        	FileUtils.deleteQuietly(logFile);
        }
	}

	/**
	 * Modules can be added / removed in some CzechIdM version.
	 *
	 * @param forVersion
	 * @return
	 */
	protected List<String> getBackendModules(String forVersion) {
		return Arrays
				.stream(BACKEND_MODULES)
				.filter(moduleName -> {
					if (moduleName.equals("tool") && forVersion.startsWith("9.7.")) {
						// added in 10.0.0
						return false;
					}
					return true;
				})
				.collect(Collectors.toList());
	}

	/**
	 * Modules can be added / removed in some CzechIdM version.
	 *
	 * @param forVersion
	 * @return
	 */
	protected List<String> getFrontendModules(String forVersion) {
		return Arrays
				.stream(FRONTEND_MODULES)
				.filter(moduleName -> {
					if (moduleName.equals("tool") && forVersion.startsWith("9.7.")) {
						// added in 10.0.0
						return false;
					}
					return true;
				})
				.collect(Collectors.toList());
	}
	
	protected CredentialsProvider getCredentialsProvider() {
		return new UsernamePasswordCredentialsProvider(username, password == null ? "" : password.asString()) {
			public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
				for (CredentialItem i : items) {
					if (i instanceof CredentialItem.Username) {
						((CredentialItem.Username) i).setValue(username);
						continue;
					}
					if (i instanceof CredentialItem.Password) {
						((CredentialItem.Password) i).setValue(password == null ? null : password.asString().toCharArray());
						continue;
					}
					if (i instanceof CredentialItem.StringType) {
						((CredentialItem.StringType) i).setValue(password == null ? null : password.asString());
						continue;
					}
					if (i instanceof CredentialItem.YesNoType) {
                        ((CredentialItem.YesNoType) i).setValue(true);
                        continue;
                    }
					throw new UnsupportedCredentialItem(uri, i.getClass().getName()
							+ ":" + i.getPromptText()); //$NON-NLS-1$
				}
				return true;
			}
		};
	}
	
	/**
	 * Append credentials to git active operations
	 * 
	 * @return
	 */
	protected TransportConfigCallback getTransportConfigCallback() {
		return new TransportConfigCallback() {
			@Override
			public void configure(Transport transport) {
				if (username == null) {
					LOG.warn("No credentials given. Set username and password, if repository authentication is needed.");
				} else {
					LOG.info("Git credentials given, username [{}].", username);
				}
				//
				transport.setCredentialsProvider(getCredentialsProvider());
				//
				if (transport instanceof SshTransport) {
					SshTransport sshTransport = (SshTransport) transport;
					//
					sshTransport.setSshSessionFactory(new JschConfigSessionFactory() {
						@Override
						protected void configure( Host host, Session session ) {
							if (password != null) {
								session.setPassword(password.asString());
								//
								LOG.info("Ssh password given, will be set for public key ...");
							}
						}
					});
				}
			}
		};
	}

	private String getFrontendModuleBasePath(String frontendModule) {
		return String.format("%s/%s", getProductRoot(), getFrontendModuleRelativePath(frontendModule));
	}

	private String getFrontendModuleRelativePath(String frontendModule) {
		return String.format("Realization/frontend/czechidm-%s", frontendModule);
	}

	private String getBackendModuleBasePath(String backendModule) {
		return String.format("%s/%s", getProductRoot(), getBackendModuleRelativePath(backendModule));
	}

	private String getBackendModuleRelativePath(String backendModule) {
		return String.format("Realization/backend/%s", backendModule);
	}

	private JsonNode getPackageJson(File modulePackage) {
		try (InputStream is = new FileInputStream(modulePackage)) {
			return mapper.readTree(IOUtils.toString(is, AttachableEntity.DEFAULT_CHARSET));
		} catch (Exception ex) {
            throw new ReleaseException(ex);
        }
	}

	private boolean isWindows() {
		return SystemUtils.IS_OS_WINDOWS;
	}
}
