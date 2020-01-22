package eu.bcvsolutions.idm.tool.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

import eu.bcvsolutions.idm.core.api.utils.ZipUtils;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.tool.exception.BuildException;

/**
 * Build project war (BE + FE).
 * 
 * @author Radek Tomiška
 * @since 10.1.0
 */
@Service
public class ProjectManager {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProjectManager.class);
	//
	private String mavenHome; // MAVEN_HOME will be used as default
	private String nodeHome;
	//
	private MavenManager mavenManager;
	
	public void init() {
		this.mavenManager = new MavenManager(mavenHome);
		// node can be installed globally => version is 
		this.nodeHome = StringUtils.isEmpty(nodeHome) ? "./npm" : nodeHome;
	}

	/**
	 * Build project.
	 * 
	 * @param rootPath
	 * @return true - project artifacts are available in dist directory
	 */
	public boolean build(String rootPath, boolean clean) {
		try {
			Assert.notNull(mavenManager, "Maven manager is not inited. Init manager before usage.");
			Assert.notNull(rootPath, "Path to folder with product and modules is required.");
			//
			File rootFolder = new File(rootPath);
			Assert.isTrue(rootFolder.exists(), String.format("Root folder [%s] not found.", rootPath));
			File productFolder = new File(rootFolder, "product");
			Assert.isTrue(productFolder.exists(), String.format("Product folder [%s] not found.", productFolder));
			File modulesFolder = new File(rootFolder, "modules");
			File frontendFolder = new File(rootFolder, "frontend");
			File targetFolder = new File(rootFolder, "target");
			File distFolder = new File(rootFolder, "dist");
			//
			// resolve product artefact
			File productWar = null;
			for (File file : productFolder.listFiles()) {
				// TODO: find last version or configurable?
				if (file.getName().endsWith(".war")) {
					productWar = file;
					break;
				}
			}
			Assert.notNull(productWar, String.format("Product artefact (.war) not found in product folder [%s].", productFolder.getPath()));
			LOG.info("Product artefact [{}] found.", productWar.getName());
			LOG.info("Clean previous target and distribution ...");
			if (clean) {
				LOG.info("Previously installed / used frontend nodejs, npm and node_modules will be deleted ...");
			}
			// clean previous target and dist
			if (targetFolder.exists()) {
				// cleanup content, which can be copied
				for (File file : targetFolder.listFiles()) {
					// prevent to delete node_modules, if clean is not used
					if (clean) {
						FileUtils.forceDelete(file);
					} else if ("npm".equals(file.getName())) {
						// nothing - leave installed npm 
					} else if ("frontend".equals(file.getName())) {
						for (File frontendFile : file.listFiles()) {
							if ("node_modules".equals(frontendFile.getName())) {
								// nothing
							} else {
								FileUtils.forceDelete(frontendFile);
							}
						}
					} else {
						FileUtils.forceDelete(file);
					}
				}
			} else {
				targetFolder.mkdirs();
			}
			if (distFolder.exists()) {
				// cleanup all content from dist folder
				for (File file : distFolder.listFiles()) {
					FileUtils.forceDelete(file);
				}
			} else {
				distFolder.mkdirs();
			}
			//
			// extract war file
			LOG.info("Product artefact [{}] will be extracted ...", productWar.getName());
			File extractedProductFolder = new File(targetFolder, "war");
			File extractedFrontendFolder = new File(new File(targetFolder, "frontend"), "fe-sources"); // has to be lower - modules are linked automatically from parent folder
			ZipUtils.extract(productWar, extractedProductFolder.getPath());
			LOG.info("Product [{}] extracted into target folder [{}].", productWar, targetFolder);
			//
			// check product version
			String productVersion = getVersion(extractedProductFolder);
			LOG.info("Product version [{}] resolved.", productVersion);
			//
			// add modules into BE libs
			// extract FE sources - prepare for build
			File productFrontendFolder = new File(String.format("%s/fe-sources", extractedProductFolder.getPath()));
			FileUtils.copyDirectory(productFrontendFolder, extractedFrontendFolder);
			//
			File productFrontendModulesFolder = new File(String.format("%s/czechidm-modules", productFrontendFolder.getPath()));
			File extractedFrontendModulesFolder = new File(String.format("%s/czechidm-modules", extractedFrontendFolder.getPath()));
			List<String> installedModules = new ArrayList<>();
			Map<String, String> distinctModuleNames = Maps.newHashMap();
			//
			if (!modulesFolder.exists()) {
				LOG.info("Folder with modules not found, modules will not be installed.");
			} else {
				for (File module : modulesFolder.listFiles()) {
					String moduleName = module.getName();
					//
					FileUtils.copyFile(module, new File(String.format("%s/WEB-INF/lib", extractedProductFolder.getPath()), moduleName));
					//
					// extract module jar into target
					File extractedModuleFolder = new File(targetFolder, FilenameUtils.removeExtension(moduleName));
						try {
						ZipUtils.extract(module, extractedModuleFolder.getPath());
						//
						String simpleModuleName = null; // module name without version suffix
						String backendModuleVersion = getVersion(extractedModuleFolder);
						if (StringUtils.isNotEmpty(backendModuleVersion)) {
							simpleModuleName = moduleName.replace(String.format("-%s", backendModuleVersion), "");
							if (distinctModuleNames.containsKey(simpleModuleName)) {
								throw new BuildException(String.format("Module [%s] cannot be installed twice. Remove one version from modules folder. Found versions [%s, %s].", 
										simpleModuleName, distinctModuleNames.get(simpleModuleName), backendModuleVersion));
							} else {
								distinctModuleNames.put(simpleModuleName, backendModuleVersion);
							}
							//
							LOG.info("Backend module [{}] instaled in version [{}].", 
									moduleName, 
									backendModuleVersion == null ? "n/a" : backendModuleVersion); // third party libraries without version in manifest ...
						} else {
							// third party libraries without version in manifest ...
							LOG.info("Backend module [{}] instaled.", moduleName); 
						}
						//
						// copy FE sources info product frontend
						File moduleFrontendFolder = new File(String.format("%s/fe-sources/czechidm-modules", extractedModuleFolder.getPath()));
						//
						if (moduleFrontendFolder.exists()) {
							// check BE vs. FE version -> throw exception otherwise
							for (File moduleFolder : moduleFrontendFolder.listFiles()) {
								File modulePackage = new File(moduleFolder.getPath(), "package.json");
								try (InputStream is = new FileInputStream(modulePackage)) {
									// FIXME: refactor super class from product / project manager (DRY - #getCurrentFrontendModuleVersion).
									JsonNode json = new ObjectMapper().readTree(IOUtils.toString(is, AttachableEntity.DEFAULT_CHARSET));
									String frontendModuleVersion = json.get("version").textValue();
									if (!StringUtils.equalsIgnoreCase(backendModuleVersion, frontendModuleVersion)) {
										throw new BuildException(String.format("Module [%s] versions differs [BE: %s] vs [FE: %s]. "
												+ "Module is wrongly released or built. Build module properly and install him again.", 
												moduleName, backendModuleVersion, frontendModuleVersion));
									}
									//
									LOG.info("Frontend module [{}] instaled in version [{}].", moduleName, frontendModuleVersion);
								} 
							}
							//
							FileUtils.copyDirectory(moduleFrontendFolder, extractedFrontendModulesFolder);
							FileUtils.copyDirectory(moduleFrontendFolder, productFrontendModulesFolder); // we need to know, what was installed in target war
						} else {
							LOG.info("Module [{}] not contain frontend.", module.getName(), getVersion(extractedModuleFolder));
						}
					} catch(ZipException ex) {
						LOG.warn("Module [] cannot be extracted, is not .jar library. Library will be installed without frontend resolving.");
					}
					//
					installedModules.add(module.getName());
				}
			}
			//
			// copy / override project frontends
			if (frontendFolder.exists() && frontendFolder.listFiles().length > 0) {
				FileUtils.copyDirectory(frontendFolder, extractedFrontendFolder);
				FileUtils.copyDirectory(frontendFolder, productFrontendFolder); // we need to know, what was installed in target war
				//
				LOG.info("Custom or overriden frontend artifacts installed {}.", Arrays.stream(frontendFolder.listFiles()).map(f -> f.getName()).collect(Collectors.toList()));
			}
			//
			// build FE - create new maven task and build
			LOG.info("Compile frontend application, this can take several minutes ...");
			FileUtils.copyInputStreamToFile(
					new ClassPathResource("eu/bcvsolutions/idm/build/fe-pom.xml").getInputStream(),
					new File(targetFolder, "pom.xml"));
			mavenManager.command(targetFolder, "clean", "package", String.format("-Dnode.home=%s", nodeHome));
			LOG.info("Frontend successfully compiled.");
			//
			// create new idm.war
			LOG.info("Build backend application with frontend included ...");
			FileUtils.copyInputStreamToFile(
					new ClassPathResource("eu/bcvsolutions/idm/build/war-pom.xml").getInputStream(),
					new File(targetFolder, "pom.xml"));
			mavenManager.command(
					targetFolder, 
					"clean", 
					"package", 
					String.format("-Dtool.version=%s", this.getClass().getPackage().getImplementationVersion()),
					String.format("-Dczechidm.version=%s", productVersion),
					String.format("-Dinstalled.modules=%s", installedModules)
					);
			LOG.info("Application successfully built.");
			//
			// move war to dist folder
			File projectWar = new File(distFolder, "idm.war");
			FileUtils.moveFile(new File(String.format("%s/target", targetFolder.getPath()), "idm.war"), projectWar);
			//
			LOG.info("Deployable project artefact [idm.war] is available in dist directory,");
			LOG.info("with installed modules {}.", installedModules);
			//
			return true;
		} catch (Exception ex) {
			throw new BuildException(ex.getLocalizedMessage(), ex);
		}
	}
	
	public void setMavenHome(String mavenHome) {
		this.mavenHome = mavenHome;
	}
	
	public void setNodeHome(String nodeHome) {
		this.nodeHome = nodeHome;
	}	
	
	private String getVersion(File library) throws FileNotFoundException, IOException {
		Assert.notNull(library, "Java library (jar, war) is required.");
		//
		String manifestPath = String.format("%s/META-INF/MANIFEST.MF", library.getPath());
		File manifestFile = new File(manifestPath);
		if (!manifestFile.exists()) {
			LOG.warn("Manifest file not found in library [{}].", library);
			//
			return null;
		}
		//
		try (InputStream manifestInputStream = new FileInputStream(manifestFile)) {
			Manifest manifest = new Manifest(manifestInputStream);
			//
			return manifest.getMainAttributes().getValue("Implementation-Version");
		}
	}
}
