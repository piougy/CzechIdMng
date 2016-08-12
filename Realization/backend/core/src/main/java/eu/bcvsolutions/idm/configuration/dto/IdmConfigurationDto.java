package eu.bcvsolutions.idm.configuration.dto;

/**
 * Configuration item
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public class IdmConfigurationDto {

	private String name;
	private String value;

	public IdmConfigurationDto() {
	}

	public IdmConfigurationDto(String name, String value) {
		this.name = name;
		this.value = value;
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
	}

}
