package eu.bcvsolutions.idm.core.workflow.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseEntityController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.domain.WorkflowDefinitionAssembler;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessDefinitionDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessDefinitionService;
import eu.bcvsolutions.idm.workflow.api.dto.WorkflowDeploymentDto;
import eu.bcvsolutions.idm.workflow.api.service.WorkflowDeploymentService;

/**
 * Rest controller for workflow task instance
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseEntityController.BASE_PATH + "/workflow/definitions")
public class WorkflowDefinitionController {

	@Autowired
	private WorkflowDeploymentService deploymentService;
	@Autowired
	private WorkflowProcessDefinitionService definitionService;
	@Value("${spring.data.rest.defaultPageSize}")
	private int defaultPageSize;

	/**
	 * Upload new deployment to Activiti engine
	 * 
	 * @param name
	 * @param fileName
	 * @param data
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST)
	public ResourceWrapper<WorkflowDeploymentDto> create(String name, String fileName, MultipartFile data)
			throws IOException {

		return new ResourceWrapper<>(deploymentService.create(name, fileName, data.getInputStream()));
	}

	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowProcessDefinitionDto>>> search(
			@RequestBody WorkflowFilterDto filter) {
		ResourcesWrapper<WorkflowProcessDefinitionDto> result = definitionService.search(filter);

		List<WorkflowProcessDefinitionDto> processes = (List<WorkflowProcessDefinitionDto>) result.getResources();
		List<ResourceWrapper<WorkflowProcessDefinitionDto>> wrappers = new ArrayList<>();

		for (WorkflowProcessDefinitionDto process : processes) {
			wrappers.add(new ResourceWrapper<WorkflowProcessDefinitionDto>(process));
		}
		ResourcesWrapper<ResourceWrapper<WorkflowProcessDefinitionDto>> resources = new ResourcesWrapper<ResourceWrapper<WorkflowProcessDefinitionDto>>(
				wrappers);
		resources.setPage(result.getPage());
		return new ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowProcessDefinitionDto>>>(resources,
				HttpStatus.OK);
	}

	/**
	 * Search all last version and active process definitions
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowProcessDefinitionDto>>> findAllProcessDefinitions() {
		List<WorkflowProcessDefinitionDto> definitions = definitionService.findAllProcessDefinitions();
		List<ResourceWrapper<WorkflowProcessDefinitionDto>> wrappers = new ArrayList<>();
		WorkflowDefinitionAssembler assembler = new WorkflowDefinitionAssembler();

		for (WorkflowProcessDefinitionDto definition : definitions) {
			wrappers.add(assembler.toResource(definition));
		}
		ResourcesWrapper<ResourceWrapper<WorkflowProcessDefinitionDto>> resources = new ResourcesWrapper<ResourceWrapper<WorkflowProcessDefinitionDto>>(
				wrappers, definitions.size());
		return new ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowProcessDefinitionDto>>>(resources,
				HttpStatus.OK);
	}

	/**
	 * Search last version and active process definitions. Use quick search api.
	 * 
	 * @param size
	 * @param page
	 * @param sort
	 * @param text
	 *            - category
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/search/quick")
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowProcessDefinitionDto>>> searchQuick(
			@RequestParam(required = false) Integer size, @RequestParam(required = false) Integer page,
			@RequestParam(required = false) String sort, @RequestParam(required = false) String category) {

		WorkflowFilterDto filter = new WorkflowFilterDto(size != null ? size : defaultPageSize);
		if (page != null) {
			filter.setPageNumber(page);
		}
		filter.setCategory(category);
		filter.initSort(sort);

		return this.search(filter);
	}

	/**
	 * Search last version process by key
	 * 
	 * @param definitionKey
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{definitionKey}")
	public ResponseEntity<ResourceWrapper<WorkflowProcessDefinitionDto>> get(@PathVariable String definitionKey) {
		WorkflowProcessDefinitionDto definition = definitionService.get(definitionKey);
		WorkflowDefinitionAssembler assembler = new WorkflowDefinitionAssembler();
		ResourceWrapper<WorkflowProcessDefinitionDto> resource = assembler.toResource(definition);

		return new ResponseEntity<ResourceWrapper<WorkflowProcessDefinitionDto>>(resource, HttpStatus.OK);
	}

	/**
	 * Generate process definition diagram image
	 * 
	 * @param definitionKey
	 * @return
	 */
	@RequestMapping(value = "/{definitionKey}/diagram", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public ResponseEntity<InputStreamResource> getDiagram(@PathVariable String definitionKey) {
		// check rights
		WorkflowProcessDefinitionDto result = definitionService.get(definitionKey);
		if (result == null) {
			throw new ResultCodeException(CoreResultCode.FORBIDDEN);
		}
		InputStream is = definitionService.getDiagramByKey(definitionKey);
		try {
			return ResponseEntity.ok().contentLength(is.available()).contentType(MediaType.IMAGE_PNG)
					.body(new InputStreamResource(is));
		} catch (IOException e) {
			throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, e);
		}
	}

}
