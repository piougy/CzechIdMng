package eu.bcvsolutions.idm.core.api.dto;

import org.springframework.hateoas.Identifiable;

/**
 * Module descriptor:
 * - adds disabled info
 * 
 * @author Radek Tomiška
 *
 */
public class ModuleDescriptorDto implements Identifiable<String> {

	private String id;
	private String name;
	private String vendor;
	private String version;
	private String description;
	private boolean disableable;
	private boolean disabled;

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public boolean isDisableable() {
		return disableable;
	}

	public void setDisableable(boolean disableable) {
		this.disableable = disableable;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
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

}
