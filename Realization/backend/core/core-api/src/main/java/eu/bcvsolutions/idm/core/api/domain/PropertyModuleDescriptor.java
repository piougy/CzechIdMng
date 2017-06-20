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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getVendorUrl() {
		return vendorUrl;
	}

	public void setVendorUrl(String vendorUrl) {
		this.vendorUrl = vendorUrl;
	}

	public String getVendorEmail() {
		return vendorEmail;
	}

	public void setVendorEmail(String vendorEmail) {
		this.vendorEmail = vendorEmail;
	}
}
