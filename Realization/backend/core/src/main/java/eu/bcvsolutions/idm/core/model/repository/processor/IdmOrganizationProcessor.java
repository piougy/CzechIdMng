package eu.bcvsolutions.idm.core.model.repository.processor;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;

import eu.bcvsolutions.idm.core.model.entity.IdmOrganization;
import eu.bcvsolutions.idm.core.rest.impl.IdmRoleController;

/**
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IdmOrganizationProcessor implements ResourceProcessor<Resource<IdmOrganization>>{

	@Override
	public Resource<IdmOrganization> process(Resource<IdmOrganization> resource) {
		String identityId = String.valueOf(resource.getContent().getId());
		
		Link revisionLink = linkTo(methodOn(IdmRoleController.class)
				.findRevisions(identityId)).withRel("revisions");
		resource.add(revisionLink);
		return resource;
	}
}
