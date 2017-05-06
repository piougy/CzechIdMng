package eu.bcvsolutions.idm.core.rest.processor;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.rest.impl.IdmIdentityController;
import eu.bcvsolutions.idm.core.rest.impl.PasswordChangeController;

/**
 * Adds links to identity
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class IdmIdentityProcessor implements ResourceProcessor<Resource<IdmIdentity>> {
	
	@Override
	public Resource<IdmIdentity> process(Resource<IdmIdentity> resource) {
		Link passwordChangeLink = linkTo(methodOn(PasswordChangeController.class)
				.passwordChange(resource.getContent().getId().toString(), null)).withRel("password-change");
		resource.add(passwordChangeLink);
		Link authoritiesLink = linkTo(methodOn(IdmIdentityController.class)
				.getGrantedAuthotrities(resource.getContent().getId().toString())).withRel("authorities");
		resource.add(authoritiesLink);
		// TODO: link to revision?
		/*Link revisionLink = linkTo(methodOn(IdmIdentityController.class)
				.findRevisions(identityUserName)).withRel("revisions");
		resource.add(revisionLink);*/
		return resource;
	}

}
