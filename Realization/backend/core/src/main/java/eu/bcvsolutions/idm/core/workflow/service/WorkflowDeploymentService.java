package eu.bcvsolutions.idm.core.workflow.service;

import java.io.InputStream;

import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowDeploymentDto;


/**
 * @author svandav
 */
public interface WorkflowDeploymentService {

	public WorkflowDeploymentDto create(String deploymentName, String fileName, InputStream inputStream);
}