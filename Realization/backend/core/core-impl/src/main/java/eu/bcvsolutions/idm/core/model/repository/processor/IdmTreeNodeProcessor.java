package eu.bcvsolutions.idm.core.model.repository.processor;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;

import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.rest.impl.IdmTreeNodeController;

/**
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IdmTreeNodeProcessor implements ResourceProcessor<Resource<IdmTreeNode>>{

	@Override
	public Resource<IdmTreeNode> process(Resource<IdmTreeNode> resource) {
		String identityId = String.valueOf(resource.getContent().getId());
		// TODO: link to revision?
		/*Link revisionLink = linkTo(methodOn(IdmTreeNodeController.class)
				.findRevisions(identityId)).withRel("revisions");
		resource.add(revisionLink);*/
		return resource;
	}
}
