package eu.bcvsolutions.idm.core.workflow.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.rest.AbstractBaseDtoService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskDefinitionDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskDefinitionService;

/**
 * Default implementation for workflow task definition service
 * @author svandav
 *
 */
@Service
public class DefaultWorkflowTaskDefinitionService  extends AbstractBaseDtoService<WorkflowTaskDefinitionDto, EmptyFilter> implements WorkflowTaskDefinitionService {

	@Autowired
	private RepositoryService repositoryService;

	
	@Override
	public List<WorkflowTaskDefinitionDto> searchTaskDefinitions(String processDefinitionId) {
		List<WorkflowTaskDefinitionDto> taskDefinitions = new ArrayList<>();
		BpmnModel model =  repositoryService.getBpmnModel(processDefinitionId);
		
		for(FlowElement element : model.getMainProcess().getFlowElements()) {
			if(element instanceof UserTask) {
				taskDefinitions.add(toResorce((UserTask) element));
			}
		}
		
		return taskDefinitions;
	}
	
	@Override
	public WorkflowTaskDefinitionDto searchTaskDefinitionById(String processDefinitionId, String taskId){
		List<WorkflowTaskDefinitionDto> tasks = this.searchTaskDefinitions(processDefinitionId);
		if(tasks != null){
			for(WorkflowTaskDefinitionDto task : tasks){
				if(task.getId().equals(taskId)){
					return task;
				}
			}
		}
		return null;
	}
	
	private WorkflowTaskDefinitionDto toResorce(UserTask task){
		WorkflowTaskDefinitionDto taskDefinitionDto = new WorkflowTaskDefinitionDto();
		
		taskDefinitionDto.setId(task.getId());
		taskDefinitionDto.setName(task.getName());
		taskDefinitionDto.setAssignee(task.getAssignee());
		taskDefinitionDto.setCandicateGroups(task.getCandidateGroups());
		taskDefinitionDto.setCandicateUsers(task.getCandidateUsers());

		return taskDefinitionDto;
	}
}
