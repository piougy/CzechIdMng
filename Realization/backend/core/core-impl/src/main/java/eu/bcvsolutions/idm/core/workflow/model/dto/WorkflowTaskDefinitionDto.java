package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.util.List;

/**
 * 
 * @author svandav
 *
 */

public class WorkflowTaskDefinitionDto {

	private String id;
	private String name;
	private String assignee;
	private List<String> candicateUsers;
	private List<String> candicateGroups;
	
	public WorkflowTaskDefinitionDto() {
	}
	
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

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	public List<String> getCandicateUsers() {
		return candicateUsers;
	}

	public void setCandicateUsers(List<String> candicateUsers) {
		this.candicateUsers = candicateUsers;
	}

	public List<String> getCandicateGroups() {
		return candicateGroups;
	}

	public void setCandicateGroups(List<String> candicateGroups) {
		this.candicateGroups = candicateGroups;
	}

}
