package eu.bcvsolutions.idm.core.model.repository.processor;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
public class IdmRoleProcessor implements ResourceProcessor<Resource<IdmRole>>{

	@Override
	public Resource<IdmRole> process(Resource<IdmRole> resource) {
		// String identityId = String.valueOf(resource.getContent().getName());
		// TODO: link to revision?
		/*Link revisionLink = linkTo(methodOn(IdmRoleController.class)
				.findRevisions(identityId)).withRel("revisions");
		resource.add(revisionLink);*/
		return resource;
	}

}
