package eu.bcvsolutions.idm.core.workflow.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.model.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Rest controller for workflow instance processes
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = "/api/workflow/processes/")
public class WorkflowProcessInstanceController {

	@Autowired
	private WorkflowProcessInstanceService workflowProcessInstanceService;
	@Autowired
	private IdmIdentityRepository idmIdentityRepository;

	/**
	 * Search instances of processes with same variables and for logged user
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "search/")
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowProcessInstanceDto>>> search(
			@RequestBody WorkflowFilterDto filter) {
		ResourcesWrapper<WorkflowProcessInstanceDto> result = workflowProcessInstanceService.search(filter);;
		List<WorkflowProcessInstanceDto> processes = (List<WorkflowProcessInstanceDto>) result.getResources();
		List<ResourceWrapper<WorkflowProcessInstanceDto>> wrappers = new ArrayList<>();
		
		for (WorkflowProcessInstanceDto process : processes) {
			wrappers.add(new ResourceWrapper<WorkflowProcessInstanceDto>(process));
		}
		ResourcesWrapper<ResourceWrapper<WorkflowProcessInstanceDto>> resources = new ResourcesWrapper<ResourceWrapper<WorkflowProcessInstanceDto>>(
				wrappers);
		resources.setPage(result.getPage());
		return new ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowProcessInstanceDto>>>(resources, HttpStatus.OK);
	}
	
	/**
	 * Search workflow processes instances for given identity username and process definition key
	 * @param size
	 * @param page
	 * @param sort
	 * @param identity
	 * @param processDefinitionKey
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "search/quick")
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowProcessInstanceDto>>> searchQuick(
			@RequestParam(required = false) int size, @RequestParam(required = false) int page, @RequestParam(required = false) String sort,  @RequestParam String identity,
			@RequestParam(required = false) String processDefinitionKey, @RequestParam(required = false) String category) {
		
		IdmIdentity idmIdentity = idmIdentityRepository.findOneByUsername(identity);
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.getEqualsVariables().put(WorkflowProcessInstanceService.APPLICANT_IDENTIFIER, idmIdentity.getId());
		filter.setProcessDefinitionKey(processDefinitionKey);
		filter.setCategory(category);
		filter.setPageNumber(page);
		filter.setPageSize(size);
		return this.search(filter);
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "{processInstanceId}")
	public ResponseEntity<WorkflowProcessInstanceDto> delete(
			@PathVariable String processInstanceId) {
		return new ResponseEntity<WorkflowProcessInstanceDto>(workflowProcessInstanceService.delete(processInstanceId, null), HttpStatus.OK);
	}


}
