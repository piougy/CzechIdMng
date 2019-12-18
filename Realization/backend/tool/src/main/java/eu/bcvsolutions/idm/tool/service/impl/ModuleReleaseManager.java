package eu.bcvsolutions.idm.tool.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.google.common.collect.Lists;

/**
 * Release module:
 * - single backend module and submodules "<module>-api", "<module>-impl" is supported.
 * - single frontend module is supported "czechidm-<module>".
 * 
 * Module has to respect CzechIdM module conventions and structure (see module archetype for more info).
 * 
 * @see ProductReleaseManager
 * @author Radek Tomiška
 * @since 10.1.0
 */
public class ModuleReleaseManager extends AbstractReleaseManager {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ModuleReleaseManager.class);
	protected static final String IDM_PREFIX = "idm-"; // module prefix => all artefacts has to start with "idm-" prefix => better find in .war/WEB-INF/libs
	//
	private String moduleId; // required e.g. rec, crt, idm-extras ...
	
	/**
	 * Repository root is same as module identifier in current folder ~ git clone in the same folder.
	 * 
	 * @param moduleId
	 */
	public ModuleReleaseManager(String moduleId) {
		this(String.format("./%s", moduleId), moduleId);
	}
	
	public ModuleReleaseManager(String repositoryRoot, String moduleId) {
		super(repositoryRoot);
		//
		Assert.hasLength(moduleId, "Module identifier is required.");
		//
		this.moduleId = moduleId;
	}
	
	@Override
	protected String getRootBackendModule() {
		String rootBackendModuleName = moduleId;
		File rootBackendFolder = new File(getBackendModuleBasePath(rootBackendModuleName));
		if (!rootBackendFolder.exists()) {
			if (rootBackendModuleName.startsWith(IDM_PREFIX)) {
				rootBackendModuleName = rootBackendModuleName.replaceFirst(IDM_PREFIX, "");
			} else {
				rootBackendModuleName = String.format("%s%s", IDM_PREFIX, rootBackendModuleName);
			}
			// => all artefacts has to start with "idm-" prefix => better find in .war/WEB-INF/libs
			rootBackendFolder = new File(getBackendModuleBasePath(rootBackendModuleName));
		}
		// required
		if (!rootBackendFolder.exists()) {
			throw new IllegalArgumentException(String.format("Backend module [%s] not found on filesystem.", moduleId));
		}
		//
		return rootBackendModuleName;
	}
	
	@Override
	protected List<String> getBackendModules(String forVersion) {
		String rootBackendModuleName = getRootBackendModule();
		List<String> backendModules = Lists.newArrayList(rootBackendModuleName);
		//
		// add optional sub modules by conventions with suffixes "-api" and "-impl"
		String subApiModuleName = String.format("%s-api", rootBackendModuleName);
		File subApiModuleFolder = new File(getBackendModuleBasePath(String.format("%s/%s", rootBackendModuleName, subApiModuleName)));
		if (subApiModuleFolder.exists()) {
			backendModules.add(String.format("%s/%s", rootBackendModuleName, subApiModuleName));
		} else {
			// try to append / remove "idm-" prefix
			if (subApiModuleName.startsWith(IDM_PREFIX)) {
				subApiModuleName = subApiModuleName.replaceFirst(IDM_PREFIX, "");
			} else {
				subApiModuleName = String.format("%s%s", IDM_PREFIX, subApiModuleName);
			}
			subApiModuleFolder = new File(getBackendModuleBasePath(String.format("%s/%s", rootBackendModuleName, subApiModuleName)));
			if (subApiModuleFolder.exists()) {
				backendModules.add(String.format("%s/%s", rootBackendModuleName, subApiModuleName));
			}
		}
		//
		String subImplModuleName = String.format("%s-impl", rootBackendModuleName);
		File subImplModuleFolder = new File(getBackendModuleBasePath(String.format("%s/%s", rootBackendModuleName, subImplModuleName)));
		if (subImplModuleFolder.exists()) {
			backendModules.add(String.format("%s/%s", rootBackendModuleName, subImplModuleName));
		} else {
			// try to append / remove "idm-" prefix
			if (subImplModuleName.startsWith(IDM_PREFIX)) {
				subImplModuleName = subImplModuleName.replaceFirst(IDM_PREFIX, "");
			} else {
				subImplModuleName = String.format("%s%s", IDM_PREFIX, subImplModuleName);
			}
			subImplModuleFolder = new File(getBackendModuleBasePath(String.format("%s/%s", rootBackendModuleName, subImplModuleName)));
			if (subImplModuleFolder.exists()) {
				backendModules.add(String.format("%s/%s", rootBackendModuleName, subImplModuleName));
			}
		}
		//
		return backendModules;
	}
	
	@Override
	protected List<String> getFrontendModules(String forVersion) {
		List<String> frontendModules = new ArrayList<>(1);
		String frontendModule = moduleId;
		if (frontendModule.startsWith(IDM_PREFIX)) {
			frontendModule = frontendModule.replaceFirst(IDM_PREFIX, "");
		}
		//
		File modulePackage = new File(String.format("%s/package.json", getFrontendModuleBasePath(frontendModule)));
		if (!modulePackage.exists()) {
			LOG.debug(String.format("Frontend module [%s] not found on filesystem, frontend will be skipped.", frontendModule));
		} else {
			frontendModules.add(frontendModule);
		}
		//
		return frontendModules;
	}
	
	@Override
	protected String mavenBuild(boolean deploy) {
		// delete node modules (~symlink to product node-modules)
		for (String frontendModule : getFrontendModules(getCurrentVersion())) {
			File symlink = new File(String.format("%s/node_modules", getFrontendModuleBasePath(frontendModule)));
			if (symlink.exists()) {
				symlink.delete();
				LOG.debug("Symlink to app node-modules deleted for module [{}].", frontendModule);
			} else {
				LOG.debug("Symlink to app node-modules for module [{}] not exists [path: {}].",
						frontendModule, symlink.getPath());
			}
		}
		//
		return super.mavenBuild(deploy);
	}
}
