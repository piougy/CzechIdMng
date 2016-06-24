package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author svandav
 *
 */

public class WorkflowTaskInstanceDto {

	private String id;
	@JsonProperty(value = "taskName")
	private String name;
	@JsonProperty(value = "taskCreated")
	private Date created;
	@JsonProperty(value = "taskAssignee")
	private String assignee;
	@JsonProperty(value = "taskDescription")
	private String description;
	private Map<String, String> variables;
	private List<FormDataDto> formData;
	private List<DecisionFormTypeDto> decisions;
	private WorkflowTaskDefinitionDto definition;
	private String applicant;
	private String applicantFullName;
	private List<IdentityLinkDto> identityLinks;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	public WorkflowTaskDefinitionDto getDefinition() {
		return definition;
	}

	public void setDefinition(WorkflowTaskDefinitionDto definition) {
		this.definition = definition;
	}

	public Map<String, String> getVariables() {
		if (variables == null) {
			variables = new HashMap<>();
		}
		return variables;
	}

	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<DecisionFormTypeDto> getDecisions() {
		if (decisions == null) {
			decisions = new ArrayList<>();
		}
		return decisions;
	}

	public void setDecisions(List<DecisionFormTypeDto> decisions) {
		this.decisions = decisions;
	}

	public List<FormDataDto> getFormData() {
		if (formData == null) {
			formData = new ArrayList<>();
		}
		return formData;
	}

	public void setFormData(List<FormDataDto> formData) {
		this.formData = formData;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getApplicant() {
		return applicant;
	}

	public void setApplicant(String applicant) {
		this.applicant = applicant;
	}

	public String getApplicantFullName() {
		return applicantFullName;
	}

	public void setApplicantFullName(String applicantFullName) {
		this.applicantFullName = applicantFullName;
	}

	public List<IdentityLinkDto> getIdentityLinks() {
		if(identityLinks == null){
			identityLinks = new ArrayList<>();
		}
		return identityLinks;
	}

	public void setIdentityLinks(List<IdentityLinkDto> identityLinks) {
		this.identityLinks = identityLinks;
	}
	

}