package eu.bcvsolutions.idm.core.workflow.rest;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.workflow.api.dto.WorkflowDeploymentDto;
import eu.bcvsolutions.idm.core.workflow.api.service.WorkflowDeploymentService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessDefinitionDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessDefinitionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Rest controller for workflow task instance
 * 
 * @author svandav
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/workflow-definitions")
@Api(
		value = WorkflowDefinitionController.TAG,  
		tags = { WorkflowDefinitionController.TAG }, 
		description = "WF definition administration",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class WorkflowDefinitionController extends AbstractReadDtoController<WorkflowProcessDefinitionDto, WorkflowFilterDto> {

	protected static final String TAG = "Workflow - definitions";
	//
	private final WorkflowDeploymentService deploymentService;
	private final WorkflowProcessDefinitionService definitionService;
	
	@Autowired
	public WorkflowDefinitionController(WorkflowProcessDefinitionService service, WorkflowDeploymentService deploymentService) {
		super(service);
		//
		Assert.notNull(deploymentService);
		//
		this.definitionService = service;
		this.deploymentService = deploymentService;
	}

	/**
	 * Search all last version and active process definitions
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_DEFINITION_READ + "')")
	@ApiOperation(
			value = "Get all definitions", 
			nickname = "findAllWorkflowDefinitions", 
			tags = { WorkflowDefinitionController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") })
				})
	public Resources<?> findAllProcessDefinitions() {
		return toResources(definitionService.findAllProcessDefinitions(), getDtoClass());
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
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_DEFINITION_READ + "')")
	@ApiOperation(
			value = "Search definitions", 
			nickname = "searchQuickWorkflowDefinitions", 
			tags = { WorkflowDefinitionController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	/**
	 * Search last version process by key
	 * 
	 * @param definitionKey
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_DEFINITION_READ + "')")
	@ApiOperation(
			value = "Workflow definition detail", 
			nickname = "getWorkflowDefinition", 
			response = WorkflowProcessDefinitionDto.class, 
			tags = { WorkflowDefinitionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.WORKFLOW_DEFINITION_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.WORKFLOW_DEFINITION_READ, description = "") })
				})
	public ResponseEntity<WorkflowProcessDefinitionDto> get(
			@ApiParam(value = "Workflow definition key.", required = true)
			@PathVariable String backendId) {
		String definitionId = definitionService.getProcessDefinitionId(backendId);
		return (ResponseEntity<WorkflowProcessDefinitionDto>) super.get(definitionId);
	}
	
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_DEFINITION_CREATE + "') or hasAuthority('" + CoreGroupPermission.WORKFLOW_DEFINITION_UPDATE + "')")
	@ApiOperation(
			value = "Create / update workflow definition", 
			nickname = "postWorkflowDefinition", 
			response = WorkflowDeploymentDto.class, 
			tags = { WorkflowDefinitionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.WORKFLOW_DEFINITION_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.WORKFLOW_DEFINITION_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.WORKFLOW_DEFINITION_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.WORKFLOW_DEFINITION_UPDATE, description = "")})
				},
			notes = "Upload new deployment to Activiti engine."
					+ " If definition with iven key exists, new deployment version is added."
					+ " All running task instances process with their deployment version."
					+ " Newly added version will be used for new instances.")
	public Resource<WorkflowDeploymentDto> post(String name, String fileName, MultipartFile data)
			throws IOException {
		WorkflowDeploymentDto deployment = deploymentService.create(name, fileName, data.getInputStream());
		Link selfLink = ControllerLinkBuilder.linkTo(this.getClass()).slash(deployment.getId()).withSelfRel();
		return new Resource<WorkflowDeploymentDto>(deployment, selfLink);
	}

	/**
	 * Generate process definition diagram image
	 * 
	 * @param definitionKey
	 * @return
	 */
	@RequestMapping(value = "/{backendId}/diagram", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_DEFINITION_READ + "')")
	@ApiOperation(
			value = "Workflow definition diagram", 
			nickname = "getWorkflowDefinitionDiagram",
			tags = { WorkflowDefinitionController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.WORKFLOW_DEFINITION_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.WORKFLOW_DEFINITION_READ, description = "") })
				},
			notes = "Returns input stream to definition's diagram.")
	public ResponseEntity<InputStreamResource> getDiagram(
			@ApiParam(value = "Workflow definition key.", required = true)
			@PathVariable String backendId) {
		// check rights
		WorkflowProcessDefinitionDto result = definitionService.getByName(backendId);
		if (result == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		InputStream is = definitionService.getDiagramByKey(backendId);
		try {
			return ResponseEntity.ok().contentLength(is.available()).contentType(MediaType.IMAGE_PNG)
					.body(new InputStreamResource(is));
		} catch (IOException e) {
			throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, e);
		}
	}

}
