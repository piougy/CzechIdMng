package eu.bcvsolutions.idm.core.model.repository.processor;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityLookup;
import eu.bcvsolutions.idm.core.rest.impl.DefaultIdmIdentityController;

@Component
public class IdmIdentityProcessor implements ResourceProcessor<Resource<IdmIdentity>> {

	@Autowired
	private IdmIdentityLookup idmIdentityLookup;
	
	@Override
	public Resource<IdmIdentity> process(Resource<IdmIdentity> resource) {
		// TODO: alps
		String identityUserName = String.valueOf(idmIdentityLookup.getResourceIdentifier(resource.getContent()));
		Link passwordChangeLink = linkTo(methodOn(DefaultIdmIdentityController.class)
				.passwordChange(identityUserName, null)).withRel("password-change");
		resource.add(passwordChangeLink);
		Link authoritiesLink = linkTo(methodOn(DefaultIdmIdentityController.class)
				.getGrantedAuthotrities(identityUserName)).withRel("authorities");
		resource.add(authoritiesLink);
		Link revisionLink = linkTo(methodOn(DefaultIdmIdentityController.class)
				.findRevisions(identityUserName)).withRel("revisions");
		resource.add(revisionLink);
		return resource;
	}

}
