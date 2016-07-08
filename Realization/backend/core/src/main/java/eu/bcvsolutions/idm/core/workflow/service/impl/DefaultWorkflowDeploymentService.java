package eu.bcvsolutions.idm.core.workflow.service.impl;

import java.io.InputStream;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowDeploymentDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowDeploymentService;

@Service
public class DefaultWorkflowDeploymentService implements WorkflowDeploymentService {

	@Autowired
	private RepositoryService repositoryService;


	@Override
	/**
	 * Upload new deployment to Activiti
	 */
	public WorkflowDeploymentDto create(String deploymentName, String fileName, InputStream inputStream) {
		Deployment deployment = repositoryService.createDeployment().addInputStream(fileName, inputStream)
				.name(deploymentName).deploy();

		return new WorkflowDeploymentDto(deployment);
	}

}
