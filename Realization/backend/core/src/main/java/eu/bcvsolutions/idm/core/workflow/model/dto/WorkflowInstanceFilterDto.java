package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.util.HashMap;
import java.util.Map;

public class WorkflowInstanceFilterDto {

	private int pageNumber = 0;
	private int pageSize = 20;
	private Map<String, Object> equalsVariables;
	private String processDefinitionId;
	private String processDefinitionKey;

	public Map<String, Object> getEqualsVariables() {
		if (equalsVariables == null) {
			equalsVariables = new HashMap<>();
		}
		return equalsVariables;
	}

	public void setEqualsVariables(Map<String, Object> equalsVariables) {
		this.equalsVariables = equalsVariables;
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

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

}
