package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.io.Serializable;
import java.util.List;

import org.springframework.hateoas.core.Relation;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * 
 * @author svandav
 *
 */
// TODO: Use in the since version 8.x.x @Relation(collectionRelation = "workflowTaskDefinitions")
@Relation(collectionRelation = "resources")
public class WorkflowTaskDefinitionDto implements BaseDto {

	private static final long serialVersionUID = 1L;

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

	@Override
	public void setId(Serializable id) {
		if (id != null) {
			Assert.isInstanceOf(String.class, id, "WorkflowTaskDefinitionDto supports only String identifier.");
		}
		this.id = (String) id;
	}

}
