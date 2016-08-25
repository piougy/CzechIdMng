package eu.bcvsolutions.idm.core.model.repository.processor;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.rest.IdmRoleController;

/**
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
public class IdmRoleProcessor implements ResourceProcessor<Resource<IdmRole>>{

	@Override
	public Resource<IdmRole> process(Resource<IdmRole> resource) {
		String identityId = String.valueOf(resource.getContent().getName());
		
		Link revisionLink = linkTo(methodOn(IdmRoleController.class)
				.findRevisions(identityId)).withRel("revisions");
		resource.add(revisionLink);
		return resource;
	}

}
