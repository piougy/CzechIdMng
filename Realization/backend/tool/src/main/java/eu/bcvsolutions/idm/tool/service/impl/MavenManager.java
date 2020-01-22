package eu.bcvsolutions.idm.tool.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.tool.exception.BuildException;

/**
 * Maven usage for build product / project.
 * 
 * 'release' profile is expected in all projects.
 * 'clean' task is called automatically before each build / install / deploy.
 * 'DdocumentationOnly' parameter is added automatically for each build / install / deploy.
 * 
 * @author Radek Tomiška
 * @since 10.1.0
 */
public class MavenManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MavenManager.class);
	//
	private enum BuildCommand {
		PACKAGE,
		INSTALL,
		DEPLOY
	}
	//
	private String mavenHome; // MAVEN_HOME will be used as default
    // cache
	private String mavenBaseCommand;
	
	public MavenManager(String mavenHome) {
		this.mavenHome = mavenHome;
		//
		String mavenBaseCommand = getMavenBaseCommand();
		LOG.debug("Maven command [{}].", mavenBaseCommand);
		Assert.isTrue(new File(mavenBaseCommand).exists(), String.format("Maven command [%s] not found.", mavenBaseCommand));
		getMavenVersion();
	}
	
	/**
	 * Build maven project.
	 * 
	 * @param projectFolder folder with maven project (contains pom.xml file).
	 * @throws BuildException execute maven command failed
	 */
	public void build(File projectFolder) {
		build(projectFolder, BuildCommand.PACKAGE);
	}
	
	/**
	 * Build and install maven project.
	 * 
	 * @param projectFolder
	 * @throws BuildException execute maven command failed
	 */
	public void install(File projectFolder) {
		build(projectFolder, BuildCommand.INSTALL);
	}
	
	/**
	 * Build and deploy maven project.
	 * 
	 * @param projectFolder
	 * @throws BuildException execute maven command failed
	 */
	public void deploy(File projectFolder) {
		build(projectFolder, BuildCommand.DEPLOY);
	}
	
	/**
	 * Execute maven command in project folder with given attributes.
	 * 
	 * @param projectFolder folder with pom.xml
	 * @param args maven arguments
	 * @throws BuildException execute maven command failed
	 */
	public void command(File projectFolder, String... args) {
		Assert.notNull(projectFolder, "Project folder is required.");
		Assert.isTrue(projectFolder.exists(), "Project folder with maven project has to exist.");
		//
		List<String> mavenArgs = Lists.newArrayList(getMavenBaseCommand());
		if (!ArrayUtils.isEmpty(args)) {
			mavenArgs.addAll(Lists.newArrayList(args));
		}
		//
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(mavenArgs);
		processBuilder.directory(projectFolder);
		execute(processBuilder, "Maven command failed");
	}
	
	protected void build(File projectFolder, BuildCommand buildCommand) {
		Assert.notNull(buildCommand, "Build command is required.");
		//
		command(projectFolder,
    			"clean",
    			buildCommand.name().toLowerCase(),
    			"-Prelease",
    			"-DdocumentationOnly=true");
	}
	
	public String getMavenVersion() {
		ProcessBuilder processBuilder = new ProcessBuilder();
    	processBuilder.command(getMavenBaseCommand(), "-v");
    	execute(processBuilder, "Check maven version failed");
    	// TODO: return maven version parsed from log
    	return null;
	}
	
	/**
	 * Execute process.
	 *
	 * @param processBuilder
	 * @param exceptionMessage
	 * @throws InterruptedException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws BuildException execute maven command failed
	 * @return log file with process execution output
	 */
	protected void execute(ProcessBuilder processBuilder, String exceptionMessage) {
		try {
	    	//
	    	Process process = processBuilder.start();
	    	// get logs
	    	BufferedReader processInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
	    	BufferedReader processError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
	    	String ligne;
	    	while ((ligne = processInput.readLine()) != null) {
	    	    LOG.debug(ligne);
	    	}
	    	while ((ligne = processError.readLine()) != null) {
	    	    LOG.error(ligne);
	    	}	    	
			// wait for end
			process.waitFor();
			// throw exception on failure
	    	if (process.exitValue() != 0) {
	    		LOG.error("Execute maven command failed [exit code: {}].", process.exitValue());
	    		//
	    		throw new BuildException(String.format("Execute maven command failed [exit code: %s].", process.exitValue()));
	    	}
		} catch (InterruptedException | IOException ex) {
			throw new BuildException(ex);
        }
	}
	
	/**
	 * Resolved executable mvn command (by given home directory and OS).
	 *
	 * @return
	 */
	private String getMavenBaseCommand() {
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
				if (SystemUtils.IS_OS_WINDOWS) {
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
}
