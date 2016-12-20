package eu.bcvsolutions.idm.core.api.dto;

/**
 * Configuration item
 * 
 * @author Radek Tomiška 
 *
 */
public class ConfigurationDto {

	private String name;
	private String value;
	private boolean secured;
	private boolean confidential;

	public ConfigurationDto() {
	}

	public ConfigurationDto(String name, String value) {
		this(name, value, false, false);
	}
	
	public ConfigurationDto(String name, String value, boolean secured, boolean confidential) {
		this.name = name;
		this.value = value;
		this.secured = secured;
		this.confidential = confidential;
	}

	/**
	 * Configuration property key
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Configuration property value
	 * 
	 * @return
	 */
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Secured property is not readable without permission. Not secured configuration property is readable without authentication 
	 * 
	 * @return
	 */
	public boolean isSecured() {
		return secured;
	}

	public void setSecured(boolean secured) {
		this.secured = secured;
	}
	
	/**
	 * Secured negate alias
	 * @return
	 */
	public boolean isPublic() {
		return !secured;
	}

	public void setPublic(boolean notSecured) {
		this.secured = !notSecured;
	}
	
	/**
	 * Confidential property - wil be saved in confidential storage
	 * 
	 * @return
	 */
	public boolean isConfidential() {
		return confidential;
	}
	
	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
		if (confidential) {
			this.secured = true;
		}
	}

}
