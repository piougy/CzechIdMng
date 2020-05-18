package eu.bcvsolutions.idm.tool.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
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
 * Release git project by maven, gulp and git:
 * - change version - all modules have to have the same version number.
 * - prepare release git branches and tags
 * - deploy into nexus
 * - https://stackoverflow.com/questions/20496084/git-status-ignore-line-endings-identical-files-windows-linux-environment
 * 
 * TODO: refactor GitManager
 * 
 * @author Radek Tomiška
 * @since 10.1.0
 */
public abstract class AbstractReleaseManager implements ReleaseManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractReleaseManager.class);
	public static String DEFAULT_REPOSITORY_LOCATION = "./CzechIdMng";
	//
	private ObjectMapper mapper;
	private MavenManager mavenManager;
	// props
	private String repositoryRoot = DEFAULT_REPOSITORY_LOCATION; // default location ./CzechIdMng will be used - git clone in the same folder
	private String developBranch = "develop";
	private String mavenHome; // MAVEN_HOME will be used as default
	private String masterBranch = "master";
	private boolean local = false; // local git repository and build only
	private boolean force = false; // skip check for count of project files changes
	private String username; // git username (for publish release)
	private GuardedString password; // git password (for publish realese) or ssh passphrase
	// cache
	private Git git;
	private PrettyPrinter prettyPrinter;
	
	public AbstractReleaseManager(String repositoryRoot) {
		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		//
		if (!StringUtils.isEmpty(repositoryRoot)) {
			this.repositoryRoot = repositoryRoot;
			//
			LOG.trace("Repository location [{}] given.", this.repositoryRoot);
		} else {
			// default location - git clone in the same folder
			this.repositoryRoot = DEFAULT_REPOSITORY_LOCATION;
			//
			LOG.trace("Default repository location [{}] will be used.", this.repositoryRoot);
		}
	}
	
	/**
	 * Project FE modules.
	 * 
	 * @param forVersion if modules differs by version (e.g some module can be added)
	 * @return FE module names without 'czechidm-' prefix.
	 */
	abstract protected List<String> getFrontendModules(String forVersion);
	
	/**
	 * Project BE modules.
	 * 
	 * @param forVersion if modules differs by version (e.g some module can be added)
	 * @return BE module names
	 */
	abstract protected List<String> getBackendModules(String forVersion);
	
	/**
	 * "Main" backend module (e.g. parent, aggregator).
	 * 
	 * @return main BE module.
	 */
	abstract protected String getRootBackendModule();
	
	@Override
	public void init() {
		mavenManager = new MavenManager(mavenHome);
		//
		String repositoryRoot = getRepositoryRoot();
		LOG.info("Repository location: [{}].", repositoryRoot);
		Assert.hasLength(repositoryRoot, "Repository root is required.");
		Assert.isTrue(new File(repositoryRoot).exists(), String.format("Repository root [%s] not found.", repositoryRoot));
		//
		LOG.info("Develop branch: [{}].", getDevelopBranch());
		LOG.info("Master branch: [{}].", getMasterBranch());
		//
		try {
			git = Git.open(new File(repositoryRoot));
		} catch (Exception ex) {
			throw new ReleaseException(String.format("Git repository in folder [%s] cannot be inited.", repositoryRoot) , ex);
		}
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
		LOG.info("Release version [{}] ...", releaseVersion);
		//
		// set stable version
		setVersion(releaseVersion);
		if (checkChanges() > 0) { // prevent to create empty commit
			gitAddAll();
			gitCommit(String.format("Release version [%s] - prepare", releaseVersion));
		}
		// deploy - its saver than merge stable into master (this takes long time and conflict can occurs)
		deploy();
		//
		if (checkChanges() > 0) { // prevent to create empty commit
			// package-lock is changed after build + deploy => we need to commit it into tag / master
			gitAddAll();
			gitCommit(String.format("Release version [%s] - alfter build", releaseVersion));
		}
		// create tag
		gitCreateTag(releaseVersion);
		// merge into master, if branch is given
		// sometimes merge is not needed (e.g. when some old hotfix branch is released - set null)
		String masterBranch = getMasterBranch();
		if (StringUtils.isNotEmpty(masterBranch)) {
			gitSwitchBranch(masterBranch);
			gitPull();
			// merge develop into master - conflict can occurs => just this and next step should be repeated.
			gitMerge(getDevelopBranch());
			// switch develop
			gitSwitchBranch(getDevelopBranch());
		}
		// set new develop version version
		if (StringUtils.isEmpty(newDevelopVersion)) {
			newDevelopVersion = getNextSnapshotVersionNumber(releaseVersion, null);
		}
		setVersion(newDevelopVersion);
		if (checkChanges() > 0) { // prevent to create empty commit
			gitAddAll();
			gitCommit(String.format("New develop version [%s]", newDevelopVersion));
		}
		//
		LOG.info("Version released [{}]. New development version [{}].", releaseVersion, newDevelopVersion);
		LOG.info("Branches [{}], [{}] and tag [{}] are prepared to push into origin.",
				getDevelopBranch(), getMasterBranch(), releaseVersion);
		//
		return releaseVersion;
	}

	@Override
	public void publish() {
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
	
	@Override
	public String releaseAndPublish(String releaseVersion, String newDevelopVersion) {
		String releasedVersion = release(releaseVersion, newDevelopVersion);
		publish();
		//
		return releasedVersion;
	}
	
	@Override
	public String build() {
		// switch develop
		gitSwitchBranch(getDevelopBranch());
		//
		String currentVersion = mavenBuild(false);
		//
		LOG.info("Version [{}] successfully built.", currentVersion);
		//
		return currentVersion;
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
		if (!isSnapshotVersion(newVersion)) {
			newVersion = String.format("%s-%s", newVersion, SNAPSHOT_VERSION_SUFFIX);
		}
		return setVersion(newVersion);
	}
	
	@Override
	public boolean isSnapshotVersion(String version) {
		Assert.hasLength(version, "Version is required.");
		//
		return version.toUpperCase().endsWith(SNAPSHOT_VERSION_SUFFIX);
	}
	
	@Override
	public String revertVersion() {
		gitSwitchBranch(getDevelopBranch());
		//
		String forVersion = getCurrentBackendModuleVersion(getRootBackendModule());
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
			gitCheckout(allProjectFiles);
			//
			String currentVersion = getCurrentVersion();
			LOG.info("Project files (pom.xml, package.json) reverted. Current version [{}].", currentVersion);
			return currentVersion;
		} catch (Exception ex) {
			throw new ReleaseException("Revert project files failed", ex);
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
	
	/**
	 * Work with local git repository only and build localy only
	 * 
	 * @param local is local
	 */
	public void setLocal(boolean local) {
		this.local = local;
	}
	
	/**
	 * Work with local git repository only and build localy only
	 * 
	 * @return is local
	 */
	public boolean isLocal() {
		return local;
	}
	
	@Override
	public String getRepositoryRoot() {
		if (StringUtils.isEmpty(repositoryRoot)) {
			repositoryRoot = DEFAULT_REPOSITORY_LOCATION;
		}
		return repositoryRoot;
	}
	
	@Override
	public void setRepositoryRoot(String repositoryRoot) {
		this.repositoryRoot = repositoryRoot;
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
	
	@Override
	public void setForce(boolean force) {
		this.force = force;
	}
	
	/**
	 * Initialized xml object mapper;
	 * 
	 * @return
	 */
	protected ObjectMapper getMapper() {
		return mapper;
	}
	
	/**
	 * Init repository - usable for tests
	 */
	protected void gitInitRepository() {
		File repositoryRoot = new File(getRepositoryRoot());
		Assert.isTrue(repositoryRoot.exists(), "Product root has to exist.");
		//
		try {
			git = Git
				.init()
				.setDirectory(repositoryRoot)
				.call();
			FileUtils.writeStringToFile(new File(repositoryRoot.getPath() + "/README.md"), "test repo", AttachableEntity.DEFAULT_CHARSET);
			gitAddAll();
			gitCommit("start commit");
		} catch (Exception ex) {
			throw new ReleaseException("Init repository failed", ex);
		}
	}
	
	/**
	 * 
	 * @param update true => ignore untracked files
	 */
	protected void gitAddAll() {
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
	
	protected void gitPull() {
		if (isLocal()) {
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

	protected String gitCreateTag(String tagVersion) {
		Assert.hasLength(tagVersion, "Tag version (~name) is required.");
		//
		boolean isSnapshotVersion = false;
		if (isSnapshotVersion(tagVersion)) {
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
		if (isLocal()) {
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
		if (isLocal()) {
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
	
	protected boolean gitIsClean() {
		try {
			Status call = git.status().call();
			//
			return call.isClean();
		} catch (NoWorkTreeException | GitAPIException ex) {
			throw new ReleaseException("Check status failed", ex);
		}
	}
	
	protected long gitAllChangesCount() {
		try {
			Status status = git.status().call();
			//
			return status.getAdded().size()
				+ status.getChanged().size()
				+ status.getRemoved().size()
				+ status.getMissing().size()
				+ status.getModified().size()
				+ status.getConflicting().size()
				+ status.getUntracked().size();
		} catch (NoWorkTreeException | GitAPIException ex) {
			throw new ReleaseException("Check status failed", ex);
		}
	}
	
	protected long checkChanges() {
		long changesCount = gitAllChangesCount();
		if (MAX_RELEASE_CHANGES < changesCount) {
			if (force) {
				LOG.warn("Count of changed files by release command [%s] exceeded, force argument is set. Limit [%s].");
			} else {
				throw new ReleaseException(String.format("Count of changed files by release command [%s] exceeded. "
						+ "Limit [%s]. "
						+ "Check changes directly in your repository (e.g. check line endings is not changed - .gitattributes with text=auto directive) "
						+ "or add --force into tool agumetr to skip this check.", 
						changesCount, MAX_RELEASE_CHANGES));
			}
		}
		//
		return changesCount;
	}
	
	protected void gitCheckout(List<String> files) {
		try {
			git
				.checkout()
				.addPaths(files)
				.call();
		} catch (Exception ex) {
			throw new ReleaseException(String.format("Revert files [%s] failed", files), ex);
		}
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
								LOG.info("Ssh passphrase given, will be set for ssh public key ...");
							}
						}
					});
				} else {
					if (username == null || password == null) {
						LOG.warn("No credentials given. Set git username and password, if repository authentication is needed.");
					} else {
						LOG.info("Git credentials given, username [{}].", username);
					}
				}
			}
		};
	}
	
	/**
	 * If module has parent defined.
	 * 
	 * @param moduleName
	 * @return true = is submodule.
	 */
	protected boolean hasParentModule(String moduleName) {
		File modulePackage = new File(String.format("%s/pom.xml", getBackendModuleBasePath(moduleName)));
		Assert.isTrue(modulePackage.exists(), String.format("Backend module [%s] not found on filesystem.", moduleName));
		//
		try {
			XmlMapper xmlMapper = new XmlMapper();
			JsonNode json = xmlMapper.readTree(modulePackage);
			//
			return json.get("parent") != null;
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
	
	protected String getCurrentFrontendModuleVersion(String moduleName) {
		File modulePackage = new File(getFrontendModuleBasePath(moduleName), "package.json");
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
	
	protected void setFrontendVersion(String fullVersionName) {
		String newVersion = fullVersionName.toLowerCase();
		//
		for (String frontendModule : getFrontendModules(newVersion)) {
			File modulePackage = new File(getFrontendModuleBasePath(frontendModule), "package.json");
			Assert.isTrue(modulePackage.exists(), String.format("Frontend module [%s] not found on filesystem.", frontendModule));
			//
			try {
				ObjectNode json = (ObjectNode) getPackageJson(modulePackage);
				json.put("version", newVersion);
				//
				try (FileOutputStream outputStream = new FileOutputStream(modulePackage)) {
					JsonGenerator jGenerator = getMapper().getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
					getMapper().writer(getPrettyPrinter()).writeValue(jGenerator, json);
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
	
	protected String getFrontendModuleBasePath(String frontendModule) {
		return String.format("%s/%s", getRepositoryRoot(), getFrontendModuleRelativePath(frontendModule));
	}

	protected String getFrontendModuleRelativePath(String frontendModule) {
		return String.format("Realization/frontend/czechidm-%s", frontendModule);
	}

	protected String getBackendModuleBasePath(String backendModule) {
		return String.format("%s/%s", getRepositoryRoot(), getBackendModuleRelativePath(backendModule));
	}

	protected String getBackendModuleRelativePath(String backendModule) {
		return String.format("Realization/backend/%s", backendModule);
	}
	
	protected JsonNode getPackageJson(File modulePackage) {
		try (InputStream is = new FileInputStream(modulePackage)) {
			return getMapper().readTree(IOUtils.toString(is, AttachableEntity.DEFAULT_CHARSET));
		} catch (Exception ex) {
            throw new ReleaseException(ex);
        }
	}
	
	protected String getCurrentVersion() {
		String currentVersion = null;
		// BE
		for (String backendModule : getBackendModules(getCurrentBackendModuleVersion(getRootBackendModule()))) {

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
	
	protected void setBackendVersion(String fullVersionName) {
		String newVersion = fullVersionName.toUpperCase();
		LOG.info("Setting backend version [{}] ...", newVersion);
		//
		String rootBackendModule = getRootBackendModule();
		String rootModulePath = getBackendModuleBasePath(rootBackendModule);
		File rootModuleFolder = new File(rootModulePath);
		//
		if (!hasParentModule(rootBackendModule)) {
			mavenManager.command(
					rootModuleFolder,
					"versions:update-parent",
					"-DparentVersion="+newVersion,
					"-DgenerateBackupPoms=false");
		}
		//
		mavenManager.command(
				rootModuleFolder,
				"versions:set",
				"-DnewVersion="+newVersion,
				"-DprocessAllModules=true",
				"-DprocessParent=false",
				"-DgenerateBackupPoms=false");
		//
		mavenManager.command(
				rootModuleFolder,
				"-N",
				"versions:update-child-modules",
				"-DgenerateBackupPoms=false");
		// FIXME: has to be executed twice ... why ...?
		mavenManager.command(
				rootModuleFolder,
				"-N",
				"versions:update-child-modules",
				"-DgenerateBackupPoms=false");
		//
		LOG.info("Backend version set to [{}]", newVersion);
	}
	
	protected String getVersionNumber(String fullVersionName) {
		return fullVersionName.toUpperCase()
				.replaceFirst("-" + SNAPSHOT_VERSION_SUFFIX, "")
				.replaceFirst("-" + RELEASE_CANDIDATE_VERSION_SUFFIX, "");
	}
	
	@Override
	public String getNextSnapshotVersionNumber(String versionNumber, VersionType versionType) {
		if (StringUtils.isEmpty(versionNumber)) {
			versionNumber = "1.0.0"; // default version
		}
		String versionNumberWithoutSnapshot = getVersionNumber(versionNumber);
		//
		String[] versionStrings = versionNumberWithoutSnapshot.split("\\.");
		List<Integer> versionNumbers = new ArrayList<>(versionStrings.length);
		String newVersion;
		try {
			for (String versionString : versionStrings) {
				versionNumbers.add(Integer.valueOf(versionString));
			}
			int incrementVersionPosition;
			if (versionType == null) {
				// last version number will be incremented by default
				incrementVersionPosition = versionNumbers.size() - 1;
			} else switch (versionType) {
				case MAJOR: {
					incrementVersionPosition = 0;
					break;
				}
				case MINOR: {
					incrementVersionPosition = 1;
					break;
				}
				case PATCH: {
					incrementVersionPosition = 2;
					break;
				}
				case HOTFIX: {
					incrementVersionPosition = 3;
					break;
				}
				default: {
					throw new UnsupportedOperationException(String.format("Version type [%s] is not supported.", versionType));
				}
			}
			// add zeros to missing version positions
			for (int i = 1; i <= incrementVersionPosition; i++) {
				if (versionNumbers.size() == incrementVersionPosition) {
					versionNumbers.add(0);
				}
			}
			// reset other versions e.g. major 2.0.0 from 1.3.4
			for (int i = incrementVersionPosition + 1; i < versionNumbers.size(); i++) {
				versionNumbers.set(i, 0);
			}
			//
			versionNumbers.set(incrementVersionPosition, versionNumbers.get(incrementVersionPosition) + 1);
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
	 * Build and deploy.
	 * 
	 * @param deploy deploy (true) or install only (false)
	 * @return built version
	 */
	protected String mavenBuild(boolean deploy) {
		if (isLocal()) {
			LOG.debug("Local repository is used - build only, deployment is not available.");
		}
		//
		String currentVersion = getCurrentVersion();
		boolean isDeploy = !isLocal() && deploy;
		//
		LOG.info("Build [true] and deploy [{}] version [{}] into nexus, this will take several minutes ...",
				isDeploy, currentVersion);
		//
		File projectFolder = new File(getBackendModuleBasePath(getRootBackendModule()));
		if (isDeploy) {
			mavenManager.deploy(projectFolder);
		} else {
			mavenManager.install(projectFolder);
		}
    	//
    	LOG.debug("Version [{}] successfully built. Deploy into nexus [{}]", currentVersion, isDeploy);
    	//
    	return currentVersion;
	}
	
	protected String deploy() {
		String currentVersion = mavenBuild(true);
		//
		LOG.info("Version [{}] successfully deployed to nexus.", currentVersion);
		//
		return currentVersion;
	}
	
	/**
	 * IdM configured json {@link PrettyPrinter} - the same format as npm is needed.
	 * 
	 * @return configured printer
	 * @since 10.3.0
	 */
	protected PrettyPrinter getPrettyPrinter() {
		if (prettyPrinter == null) {
			//
			// Prevent to append leading value space.
			DefaultPrettyPrinter defaultPrettyPrinter = new DefaultPrettyPrinter() {
	
				private static final long serialVersionUID = 1L;
	
				/**
				 * Prevent to append leading value space.
				 */
				@Override
				public DefaultPrettyPrinter withSeparators(Separators separators) {
					_separators = separators;
					_objectFieldValueSeparatorWithSpaces = separators.getObjectFieldValueSeparator() + " ";
			        return this;
				}
			};
			//
			// array value on new line
			defaultPrettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
			//
			prettyPrinter = defaultPrettyPrinter;
		}
		//
		return prettyPrinter;
	}
}
