
package eu.bcvsolutions.idm.core.workflow.model.dto;

import java.util.Date;
import org.activiti.engine.repository.Deployment;

/**
 * Dto for workflow deploy
 * 
 * @author svandav
 *
 */
public class WorkflowDeploymentDto {

	private String id;
	private String name;
	private Date deploymentTime;
	private String category;

	public WorkflowDeploymentDto() {
	}

	public WorkflowDeploymentDto(Deployment deployment) {
		this.id = deployment.getId();
		this.name = deployment.getName();
		this.category = deployment.getCategory();
		this.deploymentTime = deployment.getDeploymentTime();
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

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Date getDeploymentTime() {
		return deploymentTime;
	}

	public void setDeploymentTime(Date deploymentTime) {
		this.deploymentTime = deploymentTime;
	}
}