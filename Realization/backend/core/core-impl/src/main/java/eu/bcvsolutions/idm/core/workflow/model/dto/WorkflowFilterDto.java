package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;

public class WorkflowFilterDto extends QuickFilter {
	
	public static final String ORDER_ASC = "asc";
	public static final String ORDER_DESC = "desc";
	
	@Deprecated
	private int pageNumber = 0;
	@Deprecated
	private int pageSize = 10;
	@Deprecated
	private boolean sortAsc = false;
	@Deprecated
	private boolean sortDesc = false;
	@Deprecated
	private String sortByFields;
	
	private Map<String, Object> equalsVariables;
	private String processDefinitionId;
	private String processDefinitionKey;
	private String processInstanceId;
	private String superProcessInstanceId;
	private String name;
	private String category;
	private String candidateOrAssigned;
	private DateTime createdBefore;
	private DateTime createdAfter;

	@Deprecated
	public WorkflowFilterDto(int defaultPageSize) {
		this.pageSize = defaultPageSize;
	}
	
	public WorkflowFilterDto() {
	}
	
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

	@Deprecated
	public int getPageNumber() {
		return pageNumber;
	}

	@Deprecated
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	@Deprecated
	public int getPageSize() {
		return pageSize;
	}

	@Deprecated
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	@Deprecated
	public boolean isSortAsc() {
		return sortAsc;
	}

	@Deprecated
	public void setSortAsc(boolean sortAsc) {
		this.sortAsc = sortAsc;
	}

	@Deprecated
	public boolean isSortDesc() {
		return sortDesc;
	}

	@Deprecated
	public void setSortDesc(boolean sortDesc) {
		this.sortDesc = sortDesc;
	}

	@Deprecated
	public String getSortByFields() {
		return sortByFields;
	}

	@Deprecated
	public void setSortByFields(String sortByFields) {
		this.sortByFields = sortByFields;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String id) {
		this.processInstanceId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSuperProcessInstanceId() {
		return superProcessInstanceId;
	}

	public void setSuperProcessInstanceId(String superProcessInstanceId) {
		this.superProcessInstanceId = superProcessInstanceId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@JsonIgnore
	@Deprecated
	public void initSort(String sort) {
		if (sort == null) {
			this.setSortByFields(null);
			this.setSortAsc(true);
			this.setSortDesc(false);
			return;
		}
		String[] sorts = sort.split(",");
		if(sorts != null && sorts.length > 1){
			this.setSortByFields(sorts[0]);
			String order = sorts[sorts.length-1];
			if(WorkflowFilterDto.ORDER_ASC.equals(order)){
				this.setSortAsc(true);
			}
			if(WorkflowFilterDto.ORDER_DESC.equals(order)){
				this.setSortDesc(true);
			}
		}	
	}

	public String getCandidateOrAssigned() {
		return candidateOrAssigned;
	}

	public void setCandidateOrAssigned(String candidateOrAssigned) {
		this.candidateOrAssigned = candidateOrAssigned;
	}

	public DateTime getCreatedBefore() {
		return createdBefore;
	}

	public void setCreatedBefore(DateTime createdBefore) {
		this.createdBefore = createdBefore;
	}

	public DateTime getCreatedAfter() {
		return createdAfter;
	}

	public void setCreatedAfter(DateTime createdAfter) {
		this.createdAfter = createdAfter;
	}
}
