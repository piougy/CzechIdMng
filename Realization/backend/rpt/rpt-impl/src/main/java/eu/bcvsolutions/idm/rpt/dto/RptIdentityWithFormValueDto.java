package eu.bcvsolutions.idm.rpt.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Identity with eav value
 * 
 * @author Marek Klement
 *
 */
public class RptIdentityWithFormValueDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String firstName;
	private String lastName;
	private String username;
	private Boolean disabled;
	private String titleBefore;
	private String titleAfter;
	private String externalCode;
	private List<String> formValues;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstNameColumn) {
		this.firstName = firstNameColumn;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastNameColumn) {
		this.lastName = lastNameColumn;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	public String getTitleBefore() {
		return titleBefore;
	}

	public void setTitleBefore(String titleBefore) {
		this.titleBefore = titleBefore;
	}

	public String getTitleAfter() {
		return titleAfter;
	}

	public void setTitleAfter(String titleAfter) {
		this.titleAfter = titleAfter;
	}

	public String getExternalCode() {
		return externalCode;
	}

	public void setExternalCode(String personalNumber) {
		this.externalCode = personalNumber;
	}

	public List<String> getFormValues() {
		return formValues;
	}

	public void setFormValues(List<String> formValues) {
		this.formValues = formValues;
	}
}
