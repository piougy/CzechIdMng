package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;

/**
 * Mock dto for workflow task instance.
 *
 * Main reason for existing this DTO are bulk actions, because original task DTO
 * extends BaseDto and with this BulkAction doesn't work.
 *
 * @author Vít Švanda
 *
 */
@Relation(collectionRelation = "resources")
public class WorkflowTaskInstanceAbstractDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	@JsonProperty(value = "taskName")
	private String name;
	@JsonProperty(value = "taskAssignee")
	private String assignee;
	@JsonProperty(value = "taskDescription")
	private String description;
	private Map<String, Object> variables;
	private List<FormDataDto> formData;
	private List<DecisionFormTypeDto> decisions;
	private WorkflowTaskDefinitionDto definition;
	private String applicant;
	private String applicantFullName;
	private List<IdentityLinkDto> identityLinks;
	private String processInstanceId;
	private String processDefinitionKey;
	private String processDefinitionId;
	private int priority;
	@JsonProperty(value = PROPERTY_DTO_TYPE, access = JsonProperty.Access.READ_ONLY)
	private Class<? extends BaseDto> type = this.getClass();
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private IdmDelegationDefinitionDto delegationDefinition;
	/*
	 * From key defined custom UI component use for rendered detail this task
	 */
	private String formKey;

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

	public Map<String, Object> getVariables() {
		if (variables == null) {
			variables = new HashMap<>();
		}
		return variables;
	}

	public void setVariables(Map<String, Object> variables) {
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
		if (identityLinks == null) {
			identityLinks = new ArrayList<>();
		}
		return identityLinks;
	}

	public void setIdentityLinks(List<IdentityLinkDto> identityLinks) {
		this.identityLinks = identityLinks;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public String getFormKey() {
		return formKey;
	}

	public void setFormKey(String formKey) {
		this.formKey = formKey;
	}

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public String getProcessDefinitionKey() {
		return processDefinitionKey;
	}

	public void setProcessDefinitionKey(String processDefinitionKey) {
		this.processDefinitionKey = processDefinitionKey;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Class<? extends BaseDto> getType() {
		return type;
	}

	public IdmDelegationDefinitionDto getDelegationDefinition() {
		return delegationDefinition;
	}

	public void setDelegationDefinition(IdmDelegationDefinitionDto delegationDefinition) {
		this.delegationDefinition = delegationDefinition;
	}
}
