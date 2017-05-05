package eu.bcvsolutions.idm.core.workflow.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.api.service.LookupService;
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
@RequestMapping(value = BaseEntityController.BASE_PATH + "/workflow-processes")
public class WorkflowProcessInstanceController {

	private final LookupService entityLookupService;
	private final WorkflowProcessInstanceService workflowProcessInstanceService;
	
	@Value("${spring.data.rest.defaultPageSize}")
	private int defaultPageSize;
	
	@Autowired
	public WorkflowProcessInstanceController(
			LookupService entityLookupService,
			WorkflowProcessInstanceService workflowProcessInstanceService) {
		Assert.notNull(entityLookupService);
		Assert.notNull(workflowProcessInstanceService);
		//
		this.entityLookupService = entityLookupService;
		this.workflowProcessInstanceService = workflowProcessInstanceService;
	}

	/**
	 * Search instances of processes with same variables and for logged user
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/search")
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowProcessInstanceDto>>> search(
			@RequestBody WorkflowFilterDto filter) {
		ResourcesWrapper<WorkflowProcessInstanceDto> result = workflowProcessInstanceService.search(filter);
		;
		List<WorkflowProcessInstanceDto> processes = (List<WorkflowProcessInstanceDto>) result.getResources();
		List<ResourceWrapper<WorkflowProcessInstanceDto>> wrappers = new ArrayList<>();

		for (WorkflowProcessInstanceDto process : processes) {
			wrappers.add(new ResourceWrapper<WorkflowProcessInstanceDto>(process));
		}
		ResourcesWrapper<ResourceWrapper<WorkflowProcessInstanceDto>> resources = new ResourcesWrapper<ResourceWrapper<WorkflowProcessInstanceDto>>(
				wrappers);
		resources.setPage(result.getPage());
		return new ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowProcessInstanceDto>>>(resources,
				HttpStatus.OK);
	}

	/**
	 * Search workflow processes instances for given identity username and
	 * process definition key
	 * 
	 * @param size
	 * @param page
	 * @param sort
	 * @param identity
	 * @param processDefinitionKey
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/search/quick")
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowProcessInstanceDto>>> searchQuick(
			@RequestParam(required = false) Integer size, @RequestParam(required = false) Integer page,
			@RequestParam(required = false) String sort, @RequestParam(required = false) String identity,
			@RequestParam(required = false) String processDefinitionKey,
			@RequestParam(required = false) String category) {

		WorkflowFilterDto filter = new WorkflowFilterDto(size != null ? size : defaultPageSize);
		if (page != null) {
			filter.setPageNumber(page);
		}
		if (identity != null) {
			IdmIdentityDto identityDto = (IdmIdentityDto) entityLookupService.lookupDto(IdmIdentityDto.class, identity);
			filter.getEqualsVariables().put(WorkflowProcessInstanceService.APPLICANT_IDENTIFIER, identityDto.getId());
		}
		filter.setProcessDefinitionKey(processDefinitionKey);
		filter.setCategory(category);
		return this.search(filter);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{processInstanceId}")
	public ResponseEntity<WorkflowProcessInstanceDto> delete(@PathVariable String processInstanceId) {
		return new ResponseEntity<WorkflowProcessInstanceDto>(
				workflowProcessInstanceService.delete(processInstanceId, null), HttpStatus.OK);
	}

}
