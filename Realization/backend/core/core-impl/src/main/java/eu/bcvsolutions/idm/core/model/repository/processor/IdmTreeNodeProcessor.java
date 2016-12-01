package eu.bcvsolutions.idm.core.model.repository.processor;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;

import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;

/**
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IdmTreeNodeProcessor implements ResourceProcessor<Resource<IdmTreeNode>>{

	@Override
	public Resource<IdmTreeNode> process(Resource<IdmTreeNode> resource) {
		// String identityId = String.valueOf(resource.getContent().getId());
		// TODO: link to revision?
		/*Link revisionLink = linkTo(methodOn(IdmTreeNodeController.class)
				.findRevisions(identityId)).withRel("revisions");
		resource.add(revisionLink);*/
		return resource;
	}
}
