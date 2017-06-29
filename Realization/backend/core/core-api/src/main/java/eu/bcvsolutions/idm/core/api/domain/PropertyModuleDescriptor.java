package eu.bcvsolutions.idm.core.api.domain;

/**
 * Add default methods implementation for {@link ModuleDescriptor}. - depends on
 * module-*.properties (e.g. module-core.properties)
 * 
 * TODO: build number
 * 
 * @author Radek Tomiška
 *
 */
public abstract class PropertyModuleDescriptor extends AbstractModuleDescriptor {

	private String version;
	private String name;
	private String description;
	private String vendor;
	private String vendorUrl;
	private String vendorEmail;
	private String buildNumber;
	private String buildTimestamp;

	@Override
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	@Override
	public String getVendorUrl() {
		return vendorUrl;
	}

	public void setVendorUrl(String vendorUrl) {
		this.vendorUrl = vendorUrl;
	}

	@Override
	public String getVendorEmail() {
		return vendorEmail;
	}

	public void setVendorEmail(String vendorEmail) {
		this.vendorEmail = vendorEmail;
	}
	
	public String getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}

	public String getBuildTimestamp() {
		return buildTimestamp;
	}

	public void setBuildTimestamp(String buildTimestamp) {
		this.buildTimestamp = buildTimestamp;
	}
}
