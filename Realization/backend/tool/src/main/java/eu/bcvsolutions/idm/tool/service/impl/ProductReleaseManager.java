package eu.bcvsolutions.idm.tool.service.impl;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

/**
 * Release product by maven, gulp and git:
 * - change product version - all product modules have to have the same version number.
 * - prepare releasegit branches and tags
 * - deploy into nexus
 * - https://stackoverflow.com/questions/20496084/git-status-ignore-line-endings-identical-files-windows-linux-environment
 *
 * Manager is not thread safe, construct new manager before every usage or use a synchronized block.
 *
 * @author Radek Tomiška
 *
 */
@Service
public class ProductReleaseManager extends AbstractReleaseManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProductReleaseManager.class);
	
	/**
	 * All product backend modules.
	 */
	protected static final String[] BACKEND_MODULES = new String[]{
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
	protected static final String[] FRONTEND_MODULES = new String[]{
			"acc",
			"app",
			"core",
			"example",
			"rpt",
			"tool", // not supported for 9.7.x versions
			"vs"
	};
	
	/**
	 * Release manager with default product location.
	 */
	public ProductReleaseManager() {
		this(null);
	}

	public ProductReleaseManager(String productRoot) {
		super(productRoot);
	}
	
	@Override
	protected String getRootBackendModule() {
		return "aggregator";
	}
	
	/**
	 * Modules can be added / removed in some CzechIdM version.
	 *
	 * @param forVersion
	 * @return
	 */
	@Override
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
	@Override
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

	/**
	 * Build product.
	 * 
	 * @param deploy deploy (true) or install only (false)
	 * @return built version
	 */
	@Override
	protected String mavenBuild(boolean deploy) {
		// delete symlinks from FE folder
		for (String frontendModule : getFrontendModules(getCurrentVersion())) {
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
		//
		return super.mavenBuild(deploy);
	}
}
