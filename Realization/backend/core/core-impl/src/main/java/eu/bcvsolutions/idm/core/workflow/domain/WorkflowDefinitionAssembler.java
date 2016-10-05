package eu.bcvsolutions.idm.core.workflow.domain;

import org.springframework.hateoas.Link;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessDefinitionDto;
import eu.bcvsolutions.idm.core.workflow.rest.WorkflowDefinitionController;

/**
 * Assembler for add HATEOAS links
 * @author svandav
 *
 */
@SuppressWarnings("rawtypes")
public class WorkflowDefinitionAssembler
		extends ResourceAssemblerSupport<WorkflowProcessDefinitionDto, ResourceWrapper> {

	public WorkflowDefinitionAssembler() {
		super(WorkflowProcessDefinitionDto.class, ResourceWrapper.class);
	}

	@Override
	public ResourceWrapper<WorkflowProcessDefinitionDto> toResource(WorkflowProcessDefinitionDto entity) {
		ResourceWrapper<WorkflowProcessDefinitionDto> wrapper = new ResourceWrapper<WorkflowProcessDefinitionDto>(
				entity);
		Link selfLink = linkTo(methodOn(WorkflowDefinitionController.class).get(entity.getKey())).withSelfRel();
		wrapper.add(selfLink);
		return wrapper;
	}

}
