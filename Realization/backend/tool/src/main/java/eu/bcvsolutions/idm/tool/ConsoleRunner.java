package eu.bcvsolutions.idm.tool;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.tool.exception.BuildException;
import eu.bcvsolutions.idm.tool.service.api.ReleaseManager;
import eu.bcvsolutions.idm.tool.service.impl.ModuleReleaseManager;
import eu.bcvsolutions.idm.tool.service.impl.ProductReleaseManager;
import eu.bcvsolutions.idm.tool.service.impl.ProjectManager;

/**
 * Console entry point
 * - change product versions (BE + FE)
 * - release product version
 * 
 * FIXME: use @SpringBootApplication - dependency hell, why?
 * FIXME: use spring application + context, cleanup construct managers internaly
 * FIXME: split mega if into command implementation => registrable commands.
 * TODO: option vs. external configuration - wrap option usage.
 * 
 * @author Radek Tomiška
 *
 */
// @SpringBootApplication
public class ConsoleRunner implements CommandLineRunner {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConsoleRunner.class);
	// TODO: move to configuration api (@SpringBootApplication is needed)
	private static final String PROPERTY_PASSWORD = "idm.sec.tool.password";
	//
	@Autowired private ProductReleaseManager productReleaseManager;
	@Autowired private ModuleReleaseManager moduleReleaseManager;
	@Autowired private ProjectManager projectManager;
	// loaded external properties
	private Properties properties = null;
	
	public static void main(String [] args) {
		ConsoleRunner instance = new ConsoleRunner();
		
		
		//
		try {
			instance.run(args);
			// TODO: SpringApplication.run(ConsoleRunner.class, args); // https://github.com/spring-projects/spring-boot/issues/12456
		} catch (ResultCodeException | IllegalArgumentException ex) {
			// common exception without stack trace
			LOG.error(ex.getLocalizedMessage());
		} catch (Exception ex) {
			// unexpected exception - whole stack trace
			LOG.error("Failure", ex);
		}
	}
	
	@Override
	public void run(String... args) {
		//
		// available commands
		Option optionVersion = Option.builder("v")
	    		.longOpt("version")
	    		.desc("print tool version.")
	    		.build();
		Option optionHelp = Option.builder("h")
	    		.longOpt("help")
	    		.desc("print this message.")
	    		.build();
		Option optionBuild = Option.builder()
	    		.longOpt("build")
	    		.desc("Build project or build product only (under current develop version in develop branch).\n"
	    				+ "Maven 'install' command is used for product build, artifact will be installed into the local maven repository (=> usable as dependency for other module).\n"
	    				+ "Use '-p' argument to project build.")
	    		.build();
		Option optionRelease = Option.builder()
	    		.longOpt("release")
	    		.desc("Release product or module under '--release-version' argument. New development version will be set as '--develop-version' argument.")
	    		.build();
		Option optionPublish = Option.builder()
	    		.longOpt("publish")
	    		.desc("Push prepared development, production and tags into origin repository.")
	    		.build();
		Option optionReleaseAndPublish = Option.builder()
	    		.longOpt("release-publish")
	    		.desc("Release and publish shortcut as one command, taking the same arguments as '--release' command.")
	    		.build();
		Option optionGetVersion = Option.builder()
	    		.longOpt("get-version")
	    		.desc("Get current product version (on the development branch or set '--development-branch' argument).")
	    		.build();
		Option optionSetVersion = Option.builder()
	    		.longOpt("set-version")
	    		.desc("Set current product version (on the development branch - development branch can be changed only).")
	    		.build();
		Option optionRevertVersion = Option.builder()
	    		.longOpt("revert-version")
	    		.desc("Changed versions by release command can be reverted if needed (before commit, usable after change product version only).")
	    		.build();
		//
		// product root
		Option optionRepositoryLocation = Option.builder("r")
	    		.longOpt("repository-location")
	    		.desc("Repository root folder - should contain all modules (<repository-location>/Realization/...).\n"
	    				+ "Folder 'CzechIdMng' in the same folder as idm-tool.jar will be used as default for product.\n"
	    				+ "Folder '<module>' in the same folder as idm-tool.jar will be used as default for module.\n")
	    		.argName("path")
	            .hasArg()
	    		.build();
	    //
	    // optional arguments
		Option optionMavenHome = Option.builder()
	    		.required(false)
	    		.longOpt("maven-home")
	    		.desc("Maven home directory.\n"
	    				+ "MAVEN_HOME system property will be used as default.\n"
	    				+ "Maven directory should contain command <maven-home>/bin/mvn.")
	    		.argName("path")
	            .hasArg()
	    		.build();
		Option optionNodeHome = Option.builder()
	    		.required(false)
	    		.longOpt("node-home")
	    		.desc("Node home directory.\n"
	    				+ "Global node instalation directory should contain executable node command.\n"
	    				+ "Node and npm will be dowloaded and installed localy automaticaly into tool target folder (<target>/npm) by default otherwise.\n"
	    				+ "For Windows <node-home>/node/node.exe.\n"
	    				+ "For Linux <node-home>/node")
	    		.argName("path")
	            .hasArg()
	    		.build();
		Option optionDevelopBranch = Option.builder()
	    		.required(false)
	    		.longOpt("develop-branch")
	    		.desc("Branch with feature - working branch.\n"
	    				+ "'develop' will be used as default.")
	    		.argName("branchName")
	            .hasArg()
	    		.build();
		Option optionMasterBranch = Option.builder()
	    		.required(false)
	    		.longOpt("master-branch")
	    		.desc("Branch for releases - where feature has to be merged after release.\n"
	    				+ "'master' will be used as default.\n"
	    				+ "'none' can be given - merge don't be executed (e.g. when some old hotfix branch is released).\n")
	    		.argName("masterBranch")
	            .hasArg()
	    		.build();
		Option optionReleaseVersion = Option.builder()
	    		.required(false)
	    		.longOpt("release-version")
	    		.desc("Usable with '--release' command. Release will be create under this version.\n"
	    				+ "Stable semantic version will be used as default (=> current no snapshot version).")
	    		.argName("version")
	            .hasArg()
	    		.build();
		Option optionDevelopVersion = Option.builder()
	    		.required(false)
	    		.longOpt("develop-version")
	    		.desc("Usable with '--release' command. After release this version will be used in development branch.\n"
	    				+ "Next minor snapshot semantic version will be used as default (=> current minor snapshot version + 1).\n"
	    				+ "See major, minor, patch, hotfix argument if different version is needed.")
	    		.argName("snapshot version")
	            .hasArg()
	    		.build();
		Option optionUsername = Option.builder()
	    		.required(false)
	    		.longOpt("username")
	    		.desc("Git username, if https repitory is used. When ssh repository is used, then passphrase for ssh key is needed only.")
	    		.argName("git username")
	            .hasArg()
	    		.build();
		Option optionPassword = Option.builder()
	    		.required(false)
	    		.longOpt("password")
	    		.desc("If ssh repository is used / cloned, then passphrase for ssh key is needed only.\n"
	    				+ "If https repository is used / cloned, then git username and password is needed.\n"
	    				+ "If two-factor authntication is enabled for <username>, "
	    				+ "then token has to be given (see git documentation, how to generate authentication token for developers).\n"
	    				+ "If ssh key is used, then put passphrase for ssh key (It loads the known hosts and private keys from their "
	    				+ "default locations (identity, id_rsa and id_dsa) in the user’s .ssh directory.).")
	    		.argName("git password / token / ssh passphrase")
	            .hasArg()
	    		.build();
		//
	    // module switch
		Option optionModule = Option.builder("m")
	    		.required(false)
	    		.longOpt("module")
	    		.desc("Switch to module release / build.")
	    		.argName("moduleId")
	    		.hasArg()
	    		.build();
		//
		// project switch
		Option optionProject = Option.builder("p")
	    		.required(false)
	    		.longOpt("project")
	    		.desc("Switch to project build.")
	    		.build();
		//
		// force commit / changes is not checked
		Option optionForce = Option.builder()
	    		.required(false)
	    		.longOpt("force")
	    		.desc(String.format("Count of files changed by release command will not be checked. Limit of changed files is [%s].", ReleaseManager.MAX_RELEASE_CHANGES))
	    		.build();
		// major / minor / patch / hotfix
		Option optionMajor = Option.builder()
	    		.required(false)
	    		.longOpt("major")
	    		.desc("Next develop version will be major, e.g. release 1.2.3 => develop 2.0.0-SNAPSHOT.")
	    		.build();
		Option optionMinor = Option.builder()
	    		.required(false)
	    		.longOpt("minor")
	    		.desc("Next develop version will be minor, e.g. release 1.2.3 => develop 1.3.0-SNAPSHOT. Will be applied as default.")
	    		.build();
		Option optionPatch = Option.builder()
	    		.required(false)
	    		.longOpt("patch")
	    		.desc("Next develop version will be patch, e.g. release 1.2.3 => develop 1.2.4-SNAPSHOT.")
	    		.build();
		Option optionHotfix = Option.builder()
	    		.required(false)
	    		.longOpt("hotfix")
	    		.desc("Next develop version will be hotfix, e.g. release 1.2.3 => develop 1.2.3.1-SNAPSHOT.")
	    		.build();
		Option optionClean = Option.builder("c")
	    		.required(false)
	    		.longOpt("clean")
	    		.desc("Clean up dowloaded frontend libraries in node_modules.")
	    		.build();
		
		Options options = new Options();
		//
        // available commands
        OptionGroup commandGroup = new OptionGroup();
        commandGroup.setRequired(true);
        commandGroup.addOption(optionVersion);
        commandGroup.addOption(optionHelp);
        commandGroup.addOption(optionBuild);
        commandGroup.addOption(optionRelease);
        commandGroup.addOption(optionPublish);
        commandGroup.addOption(optionReleaseAndPublish);
        commandGroup.addOption(optionSetVersion);
        commandGroup.addOption(optionGetVersion);
        commandGroup.addOption(optionRevertVersion);
        options.addOptionGroup(commandGroup);
        //
        options.addOption(optionRepositoryLocation);
        options.addOption(optionMavenHome);
        options.addOption(optionNodeHome);
        options.addOption(optionDevelopBranch);
        options.addOption(optionMasterBranch);
        options.addOption(optionReleaseVersion);
        options.addOption(optionDevelopVersion);
        options.addOption(optionUsername);
        options.addOption(optionPassword);
        options.addOption(optionModule);
        options.addOption(optionProject);
        options.addOption(optionForce);
        options.addOption(optionMajor);
        options.addOption(optionMinor);
        options.addOption(optionPatch);
        options.addOption(optionHotfix);
        options.addOption(optionClean);
		//
		try {
			// parse arguments
			CommandLineParser parser = new DefaultParser();
			CommandLine commandLine = parser.parse(options, args, false);
			// log given arguments (for bug report, without password value)
			List<String> arguments = Arrays
					.stream(commandLine.getOptions())
					.map(option -> {
						if (!option.hasArg() ) {
							return option.getLongOpt();
						}
						return String.format(
								"%s=%s", 
								option.getLongOpt(), 
								option.getLongOpt().equals(optionPassword.getLongOpt()) ? GuardedString.SECRED_PROXY_STRING : option.getValue() // prevent to print password into logs
						); 
					})
					.collect(Collectors.toList());
			LOG.info("Running tool with arguments {}.", arguments);
			//
			boolean projectBuild = commandLine.hasOption(optionProject.getLongOpt());
			boolean releaseModule = commandLine.hasOption(optionModule.getLongOpt());
			//
			if (productReleaseManager == null) {
				// Product manager will be inited by default
				// manager's methods are used by console runner
				productReleaseManager = new ProductReleaseManager();
			}
			
			if (releaseModule && !projectBuild && moduleReleaseManager == null) { 
				moduleReleaseManager = new ModuleReleaseManager(commandLine.getOptionValue(optionModule.getLongOpt()));
			} 
			//
			if (commandLine.hasOption(optionVersion.getLongOpt())) {
				System.out.println(String.format("v%s", getVersion()));
				return;
			}
			//
			if (commandLine.hasOption(optionHelp.getLongOpt())) {
				printHelp(options);
				return;
			}
			//
			String rootFolder = null;
			if (commandLine.hasOption(optionRepositoryLocation.getLongOpt())) {
				rootFolder = commandLine.getOptionValue(optionRepositoryLocation.getLongOpt());
			}
			//
			String mavenHome = null;
			if (commandLine.hasOption(optionMavenHome.getLongOpt())) {
				mavenHome = commandLine.getOptionValue(optionMavenHome.getLongOpt());
			}
			String nodeHome = null;
			if (commandLine.hasOption(optionNodeHome.getLongOpt())) {
				nodeHome = commandLine.getOptionValue(optionNodeHome.getLongOpt());
			}
			//
			if (projectBuild) {
				if (!commandLine.hasOption(optionBuild.getLongOpt())) {
					throw new BuildException("Build a project is supported only.");
				}
				boolean clean = commandLine.hasOption(optionClean.getLongOpt());
				//
				if (projectManager == null) {
					projectManager = new ProjectManager();
					projectManager.setMavenHome(mavenHome);
					projectManager.setNodeHome(nodeHome);
					projectManager.init();
				}
				projectManager.build(rootFolder == null ? "../" : rootFolder, clean); // /tool folder by default => project is in parent folder.
				//
				LOG.info("Complete!");
				return;
			}
			//
			// Release
			ReleaseManager releaseManager = getReleaseManager(releaseModule);
			releaseManager.setMavenHome(mavenHome);
			//
			if (commandLine.hasOption(optionRepositoryLocation.getLongOpt())) {
				releaseManager.setRepositoryRoot(commandLine.getOptionValue(optionRepositoryLocation.getLongOpt()));
			}
			if (commandLine.hasOption(optionDevelopBranch.getLongOpt())) {
				releaseManager.setDevelopBranch(commandLine.getOptionValue(optionDevelopBranch.getLongOpt()));
				LOG.debug("Using develop branch [{}].", releaseManager.getDevelopBranch());
			}
			if (commandLine.hasOption(optionMasterBranch.getLongOpt())) {
				String masterBranch = commandLine.getOptionValue(optionMasterBranch.getLongOpt());
				if (masterBranch.equals("none")) {
					masterBranch = null;
				}
				releaseManager.setMasterBranch(masterBranch);
				LOG.debug("Using production branch [{}].", releaseManager.getMasterBranch());
			}
			if (commandLine.hasOption(optionUsername.getLongOpt())) {
				String username = commandLine.getOptionValue(optionUsername.getLongOpt());
				releaseManager.setUsername(username);
				LOG.debug("Using git username [{}].", username);
			}
			//
			GuardedString password = null;
			if (commandLine.hasOption(optionPassword.getLongOpt())) {
				password = new GuardedString(commandLine.getOptionValue(optionPassword.getLongOpt()));
				
			} else {
				// get password from file
				String externalPassword = getProperty(PROPERTY_PASSWORD);
				if (StringUtils.isNotEmpty(externalPassword)) {
					password = new GuardedString(externalPassword);
				} else if (commandLine.hasOption(optionRelease.getLongOpt()) 
						|| commandLine.hasOption(optionReleaseAndPublish.getLongOpt())) {
					// prompt when publish / release-publish command is used
					// creates a console object
					Console cnsl = System.console();
			        if (cnsl != null) {
			        	System.out.println(optionPassword.getDescription());
						char[] pwd = cnsl.readPassword(String.format("%s: ", optionPassword.getArgName()));
						if (pwd != null && pwd.length > 0) {
							password = new GuardedString(new String(pwd));
						}
			        }
				}
			}
			if (password != null) {
				releaseManager.setPassword(password);
				LOG.info(String.format("Password (%s) is given.", optionPassword.getArgName()));	
			}
			
			//
			if (commandLine.hasOption(optionForce.getLongOpt())) {
				releaseManager.setForce(true);
				LOG.debug("Force argument was given, count of files changed by release command will not be checked.");
			}
			// before run - check props is set
			releaseManager.init();
			//
			if (commandLine.hasOption(optionBuild.getLongOpt())) {
				String currentVersion = releaseManager.build();
				//
				LOG.info("Product version [{}] successfully built and installed into local maven repository.", currentVersion);
			} else if (commandLine.hasOption(optionRelease.getLongOpt()) || commandLine.hasOption(optionReleaseAndPublish.getLongOpt())) {
				String releaseVersion = commandLine.getOptionValue(optionReleaseVersion.getLongOpt());
				String developVersion = commandLine.getOptionValue(optionDevelopVersion.getLongOpt());
				String currentVersion = releaseManager.getCurrentVersion(null); // current [snapshot] develop version 
				//
				if (StringUtils.isEmpty(developVersion)) {
					// prepare next development version by major / minor / patch / hotfix switch
					ReleaseManager.VersionType versionType = null;
					if (commandLine.hasOption(optionMajor.getLongOpt())) {
						versionType = ReleaseManager.VersionType.MAJOR;
					}
					if (commandLine.hasOption(optionMinor.getLongOpt())) {
						versionType = ReleaseManager.VersionType.MINOR;
					}
					if (commandLine.hasOption(optionPatch.getLongOpt())) {
						versionType = ReleaseManager.VersionType.PATCH;
					}
					if (commandLine.hasOption(optionHotfix.getLongOpt())) {
						versionType = ReleaseManager.VersionType.HOTFIX;
					}
					//
					if (versionType == null) {
						// minor as default
						versionType = ReleaseManager.VersionType.MINOR;
					}
					developVersion = releaseManager.getNextSnapshotVersionNumber(
							StringUtils.isEmpty(releaseVersion) ? currentVersion : releaseVersion, 
							versionType
					);
				}
				//
				String releasedVersion = releaseManager.release(releaseVersion, developVersion);
				//
				LOG.info("Product version released [{}]. New development version [{}].", releasedVersion, currentVersion);
				LOG.info("Branches [{}], [{}] and tag [{}] are prepared to push into origin (use --publish command).",
						releaseManager.getDevelopBranch(), releaseManager.getMasterBranch(), releasedVersion);
				// publish shortcut after release
				if (commandLine.hasOption(optionReleaseAndPublish.getLongOpt())) {
					releaseManager.publish();
					
					LOG.info("Branches [{}], [{}] and prepared tags pushed into origin.",
							releaseManager.getDevelopBranch(), releaseManager.getMasterBranch());
				}
			} else if (commandLine.hasOption(optionPublish.getLongOpt())) {
				// standalone publish
				releaseManager.publish();
				
				LOG.info("Branches [{}], [{}] and prepared tags pushed into origin.",
						releaseManager.getDevelopBranch(), releaseManager.getMasterBranch());
				
			} else if (commandLine.hasOption(optionRevertVersion.getLongOpt())) {
				
				LOG.info("Current product version [{}].", releaseManager.revertVersion());
				
			} else if (commandLine.hasOption(optionSetVersion.getLongOpt())) {
				
				String developVersion = commandLine.getOptionValue(optionDevelopVersion.getLongOpt());
				//
				LOG.info("Current product version [{}].", releaseManager.setVersion(developVersion));
				
			} else if (commandLine.hasOption(optionGetVersion.getLongOpt())) {
				
				String branch = null;
				if (commandLine.hasOption(optionDevelopBranch.getLongOpt())) {
					branch = commandLine.getOptionValue(optionDevelopBranch.getLongOpt());
				}
				String currentVersion = releaseManager.getCurrentVersion(branch);
				//
				LOG.info("Current product version [{}].", currentVersion);
				
			}
			//
			LOG.info("Complete!");
		} catch (ParseException | IOException ex) {
			LOG.error(ex.getLocalizedMessage());
		}
	}
	
	protected ReleaseManager getReleaseManager(boolean releaseModule) {
		return releaseModule ? moduleReleaseManager : productReleaseManager;
	}
	
	protected void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		String version = getVersion();
		//
		formatter.printHelp(
				175, 
				"java -jar idm-tool.jar", 
				String.format("CzechIdm tool version [%s], arguments:\n", version), 
				options,
				productReleaseManager.isSnapshotVersion(version)
				?
				"\nRead more in documentation [https://github.com/bcvsolutions/CzechIdMng/blob/develop/Realization/backend/tool/README.md]."
				:
				String.format("\nRead more in documentation [https://github.com/bcvsolutions/CzechIdMng/blob/%s/Realization/backend/tool/README.md].", version)
		);
	}
	
	private Properties getProperties() throws IOException {
		if (properties == null) {
			File configFile = new File("application.properties");
			if (configFile.exists()) {
				try (InputStream input = new FileInputStream(configFile)) {
					properties = new Properties();
		            // load a properties file
					properties.load(input);
					//
					LOG.info("Property file [application.properties] found. External configuration will be used.");
		        }
			}
		}
		//
		return properties;
	}
	
	private String getProperty(String propertyName) throws IOException {
		Properties properties = getProperties();
		if (properties == null) {
			return null;
		}
		//
		String propertyValue = properties.getProperty(propertyName);
		if (!StringUtils.isEmpty(propertyValue)) {
			LOG.info("Property [{}] from configuration file will be used.", propertyName);
			//
			return propertyValue;
		}
		//
		return null;
	}
	
	/**
	 * Version is available after tool is properly built.
	 * 
	 * @return
	 */
	private String getVersion() {
		return this.getClass().getPackage().getImplementationVersion();
	}
}
