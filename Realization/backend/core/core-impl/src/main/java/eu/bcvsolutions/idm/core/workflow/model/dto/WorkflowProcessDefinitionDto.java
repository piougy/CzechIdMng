package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.hateoas.core.Relation;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * Dto for workflow process definition
 * 
 * @author svandav
 *
 */
@Relation(collectionRelation = "workflowProcessDefinitions")
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowProcessDefinitionDto implements BaseDto {

	private static final long serialVersionUID = 1L;
	private String id;
	private String key;
	private String name;
	private Integer version;
	private String description;
	private String category;
	private String resourceName;
	private String deploymentId;
	private String diagramResourceName;
	private boolean hasStartFormKey;
	private boolean hasGraphicalNotation;
	private boolean suspended;
	private String tenantId;

	public WorkflowProcessDefinitionDto() {
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	public String getDiagramResourceName() {
		return diagramResourceName;
	}

	public void setDiagramResourceName(String diagramResourceName) {
		this.diagramResourceName = diagramResourceName;
	}

	public boolean hasStartFormKey() {
		return hasStartFormKey;
	}

	public void setStartFormKey(boolean hasStartFormKey) {
		this.hasStartFormKey = hasStartFormKey;
	}

	public boolean hasGraphicalNotation() {
		return hasGraphicalNotation;
	}

	public void setGraphicalNotation(boolean hasGraphicalNotation) {
		this.hasGraphicalNotation = hasGraphicalNotation;
	}

	public boolean isSuspended() {
		return suspended;
	}

	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(Serializable id) {
		if (id != null) {
			Assert.isInstanceOf(String.class, id, "WorkflowProcessDefinitionDto supports only String identifier.");
		}
		this.id = (String) id;
	}
}
