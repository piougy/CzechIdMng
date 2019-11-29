package eu.bcvsolutions.idm.tool;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.tool.service.api.ReleaseManager;
import eu.bcvsolutions.idm.tool.service.impl.DefaultReleaseManager;

/**
 * Console entry point
 * - change product versions (BE + FE)
 * - release product version
 * 
 * TODO: use some properties 
 * 
 * @author Radek Tomiška
 *
 */
public class ConsoleRunner {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConsoleRunner.class);
	//
	@Autowired private ReleaseManager releaseManager;
	
	public static void main(String [] args) {
		ConsoleRunner instance = new ConsoleRunner();
		instance.releaseManager = new DefaultReleaseManager();
		try {
			instance.run(args);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@SuppressWarnings("static-access")
	public void run(String [] args) {
		// available commands
		Option optionHelp = OptionBuilder
        		.withLongOpt("help")
        		.withDescription("print this message.")
        		.create("h");
		Option optionRelease = OptionBuilder
        		.withLongOpt("release")
        		.withDescription("release product under 'releaseVersion' argument. New development version will be set as 'developVersion' argument.")
        		.create();
		Option optionPublish = OptionBuilder
        		.withLongOpt("publish")
        		.withDescription("Push prepared development, production and tags into origin repository.")
        		.create();
		Option optionGetVersion = OptionBuilder
        		.withLongOpt("getVersion")
        		.withDescription("Get current product version (on the development branch or set 'developmentBranch' argument).")
        		.create();
		Option optionSetVersion = OptionBuilder
        		.withLongOpt("setVersion")
        		.withDescription("Set current product version (on the development branch - development branch can be changed only).")
        		.create();
		Option optionRevertVersion = OptionBuilder
        		.withLongOpt("revertVersion")
        		.withDescription("Revert (~checkout) product project files (pom.xml, package.json).")
        		.create();
		//
		// product root
        Option optionProductLocaltion = OptionBuilder
        		.withLongOpt("productLocation")
        		.withDescription("Product root folder - should contain all product modules (<productLocation>/Realization/...).\n"
        				+ "Folder 'CzechIdMng' in the same folder as idm-tool.jar will be used as default.")
        		.withArgName("path")
                .hasArg()
        		.create("p");
        //
        // optional arguments
        Option optionMavenHome = OptionBuilder
        		.isRequired(false)
        		.withLongOpt("mavenHome")
        		.withDescription("Maven home directory.\n"
        				+ "MAVEN_HOME system property will be used as default.\n"
        				+ "Maven directory should contain command <mavenHome>/bin/mvn.")
        		.withArgName("path")
                .hasArg()
        		.create();
        Option optionDevelopBranch = OptionBuilder
        		.isRequired(false)
        		.withLongOpt("developBranch")
        		.withDescription("Branch with feature - working branch.\n"
        				+ "'develop' will be used as default.")
        		.withArgName("branchName")
                .hasArg()
        		.create();
        Option optionMasterBranch = OptionBuilder
        		.isRequired(false)
        		.withLongOpt("masterBranch")
        		.withDescription("Branch for releases - where feature has to be merged after release.\n"
        				+ "'master' will be used as default.\n"
        				+ "'none' can be given - merge don't be executed (e.g. when some old hotfix branch is released)..\n")
        		.withArgName("masterBranch")
                .hasArg()
        		.create();
        Option optionReleaseVersion = OptionBuilder
        		.isRequired(false)
        		.withLongOpt("releaseVersion")
        		.withDescription("Usable with 'release' command. Release will be create under this version.\n"
        				+ "Stable semantic version will be used as default (=> current no snapshot version).")
        		.withArgName("version")
                .hasArg()
        		.create();
        Option optionDevelopVersion = OptionBuilder
        		.isRequired(false)
        		.withLongOpt("developVersion")
        		.withDescription("Usable with 'release' command. After release this version will be used in development branch.\n"
        				+ "Next snapshot semantic version will be used as default (=> current snapshot version + 1).")
        		.withArgName("snapshot version")
                .hasArg()
        		.create();
        Option optionUsername = OptionBuilder
        		.isRequired(false)
        		.withLongOpt("username")
        		.withDescription("Usable with 'publish' command. Git credentials.")
        		.withArgName("git username")
                .hasArg()
        		.create();
        Option optionPassword = OptionBuilder
        		.isRequired(false)
        		.withLongOpt("password")
        		.withDescription("Usable with 'publish' command. Git credentials.\n"
        				+ "If two-factor authntication is enabled for <username>, "
        				+ "then token has to be given (see git documentation, how to generate authentication token for developers).\n"
        				+ "If ssh key is used, then put passphrase for ssh key (It loads the known hosts and private keys from their "
        				+ "default locations (identity, id_rsa and id_dsa) in the user’s .ssh directory.).")
        		.withArgName("git password / token / ssh passphrase ")
                .hasArg()
        		.create();
        //
		Options options = new Options();
		
        // available commands
        OptionGroup commandGroup = new OptionGroup();
        commandGroup.setRequired(true);
        commandGroup.addOption(optionHelp);
        commandGroup.addOption(optionRelease);
        commandGroup.addOption(optionPublish);
        commandGroup.addOption(optionSetVersion);
        commandGroup.addOption(optionGetVersion);
        commandGroup.addOption(optionRevertVersion);
        options.addOptionGroup(commandGroup);
        //
        options.addOption(optionProductLocaltion);
        options.addOption(optionMavenHome);
        options.addOption(optionDevelopBranch);
        options.addOption(optionMasterBranch);
        options.addOption(optionReleaseVersion);
        options.addOption(optionDevelopVersion);
        options.addOption(optionUsername);
        options.addOption(optionPassword);
		//
		try {
			CommandLineParser parser = new BasicParser();
			CommandLine commandLine = parser.parse(options, args, false);
			if (commandLine.hasOption(optionHelp.getLongOpt())) {
				printHelp(options);
				return;
			}
			//
			if (commandLine.hasOption(optionProductLocaltion.getLongOpt())) {
				releaseManager.setProductRoot(commandLine.getOptionValue(optionProductLocaltion.getLongOpt()));
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
			if (commandLine.hasOption(optionPassword.getLongOpt())) {
				releaseManager.setPassword(new GuardedString(commandLine.getOptionValue(optionPassword.getLongOpt())));
				LOG.debug("Git password is given too.");
			}
			// before run - check props is set
			releaseManager.init();
			//
			if (commandLine.hasOption(optionRelease.getLongOpt())) {
				String releaseVersion = commandLine.getOptionValue(optionReleaseVersion.getLongOpt());
				String developVersion = commandLine.getOptionValue(optionDevelopVersion.getLongOpt());
				//
				String releasedVersion = releaseManager.release(releaseVersion, developVersion);
				String currentVersion = releaseManager.getCurrentVersion(null);
				//
				LOG.info("Product version released [{}]. New development version [{}].", releasedVersion, currentVersion);
				LOG.info("Branches [{}], [{}] and tag [{}] are prepared to push into origin (use --publish command).",
						releaseManager.getDevelopBranch(), releaseManager.getMasterBranch(), releasedVersion);
				
			} else if (commandLine.hasOption(optionPublish.getLongOpt())) {
				
				releaseManager.publishRelease();
				
				LOG.info("Branches [{}], [{}] and prepared tags pushed into origin.",
						releaseManager.getDevelopBranch(), releaseManager.getMasterBranch());
				
			} else if (commandLine.hasOption(optionRevertVersion.getLongOpt())) {
				
				LOG.info("Current product version [{}].", releaseManager.revertRelease());
				
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
				
			} else {
				
				LOG.error("Command [{}] not supported.");
				printHelp(options);
				
			}
			//
			LOG.info("Complete!");
		} catch (ParseException ex) {
			ex.printStackTrace();
			printHelp(options);
		}
	}
	
	protected void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(150, "java -jar idm-tool.jar", null, options, null);
	}
}
