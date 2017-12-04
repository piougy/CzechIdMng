package eu.bcvsolutions.idm.core.api.dto;

/**
 * Module descriptor:
 * - adds disabled info
 * 
 * @author Radek Tomiška
 */
public class ModuleDescriptorDto extends AbstractComponentDto {

	private static final long serialVersionUID = 1L;
	//
	private String vendor;
	private String version;
	private String vendorUrl;
	private String vendorEmail;
	private String buildNumber;
	private String buildTimestamp;
	private boolean disableable;
	private boolean documentationAvailable;
	
	@Override
	public String getModule() {
		// id = module
		return getId();
	}

	public boolean isDisableable() {
		return disableable;
	}

	public void setDisableable(boolean disableable) {
		this.disableable = disableable;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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
	
	public boolean isDocumentationAvailable() {
		return documentationAvailable;
	}
	
	public void setDocumentationAvailable(boolean documentationAvailable) {
		this.documentationAvailable = documentationAvailable;
	}
}
