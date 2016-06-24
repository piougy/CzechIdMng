package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.util.HashMap;
import java.util.Map;

public class FormDataWrapperDto {
	
	private String decision;

	private Map<String, String> formData;
	
	public String getDecision() {
		return decision;
	}

	public void setDecision(String decision) {
		this.decision = decision;
	}

	public Map<String, String> getFormData() {
		if(formData == null){
			formData = new HashMap<>();
		}
		return formData;
	}

	public void setFormData(Map<String, String> formData) {
		this.formData = formData;
	}
	
}
