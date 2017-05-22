package eu.bcvsolutions.idm.core.workflow.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.workflow.api.dto.WorkflowDeploymentDto;
import eu.bcvsolutions.idm.core.workflow.api.service.WorkflowDeploymentService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessDefinitionDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessDefinitionService;

/**
 * Rest controller for workflow task instance
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/workflow-definitions")
public class WorkflowDefinitionController extends AbstractReadWriteDtoController<WorkflowProcessDefinitionDto, WorkflowFilterDto> {

	@Autowired
	public WorkflowDefinitionController(WorkflowProcessDefinitionService service) {
		super(service);
	}

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
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public Resource<WorkflowDeploymentDto> create(String name, String fileName, MultipartFile data)
			throws IOException {
		WorkflowDeploymentDto deployment = deploymentService.create(name, fileName, data.getInputStream());
		Link selfLink = ControllerLinkBuilder.linkTo(this.getClass()).slash(deployment.getId()).withSelfRel();
		return new Resource<WorkflowDeploymentDto>(deployment, selfLink);
	}

	/**
	 * Search all last version and active process definitions
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public Resources<WorkflowProcessDefinitionDto> findAllProcessDefinitions() {
		List<WorkflowProcessDefinitionDto> definitions = definitionService.findAllProcessDefinitions();
		return (Resources<WorkflowProcessDefinitionDto>) toResources(definitions, getDtoClass());
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
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	public Resources<?> findQuick(@RequestParam MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	

	/**
	 * Search last version process by key
	 * 
	 * @param definitionKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{definitionKey}")
	public ResponseEntity<WorkflowProcessDefinitionDto> get(@PathVariable String definitionKey) {
		String definitionId = definitionService.getProcessDefinitionId(definitionKey);
		return (ResponseEntity<WorkflowProcessDefinitionDto>) super.get(definitionId);
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
		WorkflowProcessDefinitionDto result = definitionService.getByName(definitionKey);
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
