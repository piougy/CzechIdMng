package eu.bcvsolutions.idm.configuration.dto;

/**
 * Configuration item
 * 
 * TODO: include guarded string to guarded properties
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public class ConfigurationDto {

	private String name;
	private String value;
	private boolean secured;

	public ConfigurationDto() {
	}

	public ConfigurationDto(String name, String value) {
		this(name, value, false);
	}
	
	public ConfigurationDto(String name, String value, boolean secured) {
		this.name = name;
		this.value = value;
		this.secured = secured;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}public void setSecured(boolean secured) {
		this.secured = secured;
	}
	
	public boolean isSecured() {
		return secured;
	}

}
