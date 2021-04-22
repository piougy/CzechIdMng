package eu.bcvsolutions.idm.tool.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
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
	private static final String MAVEN_PROJECT_FILE_NAME = "pom.xml";
	//
	private enum BuildCommand {
		PACKAGE,
		INSTALL,
		DEPLOY
	}
	//
	private String mavenHome; // MAVEN_HOME will be used as default
	private MavenXpp3Reader mavenReader = new MavenXpp3Reader(); // read maven poms
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
	 * Install library into local maven repository.
	 * 
	 * @param pomFile library (~module) maven project to install
	 * @param library [optional ] jar library (~module) to install
	 * @since 10.7.0
	 * @return installed model
	 */
	public Model installFile(File pomFile, File jarLibrary) {
		Assert.notNull(pomFile, "Library (jar / pom) is required.");
		//
		Model model = getModel(pomFile);
		if (model == null) {
			LOG.warn("Library [{}] cannot be installed into local maven repository for backend build, "
					+ "library is not built by maven.",
					pomFile.getPath());
			//
			return null;
		}
		//
		// validation
		String version = getVersion(model);
		String groupId = getGroupId(model);
		Assert.hasLength(version, "Library version is required, library is not properly compiled. Build library again a try to install file.");
		Assert.hasLength(groupId, "Library groupId is required, library is not properly compiled. Build library again a try to install file.");
		//
		//
		// pom / jar library is supported
		if (jarLibrary == null) {
			command(pomFile.getParentFile(),
	    			"install:install-file",
	    			String.format("-Dfile=%s", pomFile.getName()),
	    			String.format("-DgroupId=%s", groupId),
	    			String.format("-DartifactId=%s", model.getArtifactId()),
	    			String.format("-Dversion=%s", version),
	    			String.format("-Dpackaging=%s", model.getPackaging()));
		} else {
			try {
				command(jarLibrary.getParentFile(),
		    			"install:install-file",
		    			String.format("-Dfile=%s", jarLibrary.getName()),
		    			String.format("-DgroupId=%s", groupId),
		    			String.format("-DartifactId=%s", model.getArtifactId()),
		    			String.format("-Dversion=%s", version),
		    			String.format("-Dpackaging=%s", model.getPackaging()),
		    			String.format("-DpomFile=%s", pomFile.getAbsolutePath()));
			} catch (Exception ex) {
				LOG.warn("Library [{}] cannot be installed into local maven repository for backend build, "
						+ "library is not built by maven.",
						jarLibrary.getName());
				//
				return null;
			}
		}
		//
		LOG.info("Artefact [{}:{}:{}] installed into local maven repository for backend build.",
				groupId, model.getArtifactId(), version);
		//
		return model;
	}
	
	/**
	 * Resolve maven model from given library. Pom or jar library is supported only.
	 * Jar library has to contain standard maven pom.xml descriptor.
	 * 
	 * @param extractedLibrary extracted library (~module) folder
	 * @return maven model
	 * @since 10.7.0
	 */
	public Model getModel(File extractedLibrary) {
		Assert.notNull(extractedLibrary, "Library (jar / pom) is required.");
		File pomFile = null;
		//
		// pom / jar library is supported
		if (extractedLibrary.getName().equals(MAVEN_PROJECT_FILE_NAME)) {
			pomFile = extractedLibrary;
		} else {		
			File moduleMavenResources = new File(String.format("%s/META-INF/maven", extractedLibrary.getPath()));
			if (!moduleMavenResources.exists()) {
				LOG.info("Library [{}] cannot be resolved as maven dependency for backend build, "
						+ "required maven descriptor [{}] not found.",
						extractedLibrary.getName(), MAVEN_PROJECT_FILE_NAME);
				return null;
			} 	
			//
	        for (File file : FileUtils.listFiles(moduleMavenResources, null, true)) {
	            if (file.getName().equals(MAVEN_PROJECT_FILE_NAME)) { // find the first file, more projects are not supported now
	            	pomFile = file;
	    			break;
	            }
	        }
		}
        // pom file not resolved
        if (pomFile == null) {
        	LOG.info("Library [{}] cannot be resolved as maven dependency for backend build, "
					+ "required maven descriptor [{}] not found.",
					extractedLibrary.getName(), MAVEN_PROJECT_FILE_NAME);
			return null;
        }
        // read model from pom file
        try (InputStream is = new FileInputStream(pomFile)) {
			Model model = mavenReader.read(is);
			model.setPomFile(pomFile);
			return model;
		} catch (IOException | XmlPullParserException ex) {
			LOG.warn("Library [{}] cannot be resolved as maven dependency for backend build, "
					+ "required maven descriptor [{}] is not readable.",
					extractedLibrary.getName(), MAVEN_PROJECT_FILE_NAME, ex);
			return null;
		}
	}
	
	/**
	 * Get model (~ module) groupId.
	 * @param model maven model
	 * @return groupId
	 * @since 10.7.0
	 */
	public String getGroupId(Model model) {
		Parent parent = model.getParent();
		String groupId = model.getGroupId();
		if (StringUtils.isEmpty(groupId) && parent != null) {
			groupId = parent.getGroupId();
		}
		//
		return groupId;
	}
	
	/**
	 * Get model (~ module) version.
	 * 
	 * @param model maven model
	 * @return version
	 * @since 10.7.0
	 */
	public String getVersion(Model model) {
		Parent parent = model.getParent();
		String version = model.getVersion();
		// module defined
		if (StringUtils.isNotEmpty(version)) {
			return version;
		}	
		// try from parent
		if (parent == null) {
			return null;
		} 
		//
		return parent.getVersion();
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
	
	public final String getMavenVersion() {
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
	    	    LOG.info(ligne);
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
	private final String getMavenBaseCommand() {
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
