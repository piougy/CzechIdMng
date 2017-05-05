package eu.bcvsolutions.idm.core.workflow.api.service;

import java.io.InputStream;

import eu.bcvsolutions.idm.core.workflow.api.dto.WorkflowDeploymentDto;


/**
 * Service for control workflow deployments.
 *
 * @author svandav
 */
public interface WorkflowDeploymentService {

	/**
	 * Create deployment. If file name end on "bpmn20.xml", then will be deployed as workflow definition.
     *
	 * @param deploymentName
	 * @param fileName
	 * @param inputStream
	 * @return
	 */
	public WorkflowDeploymentDto create(String deploymentName, String fileName, InputStream inputStream);
}