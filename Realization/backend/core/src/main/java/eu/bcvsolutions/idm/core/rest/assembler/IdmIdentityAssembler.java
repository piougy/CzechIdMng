package eu.bcvsolutions.idm.core.rest.assembler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.support.SelfLinkProvider;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.rest.impl.IdmIdentityController;

/**
 * Identity assembler
 * 
 * @author Radek Tomiška
 *
 */
@Component
@SuppressWarnings("rawtypes")
public class IdmIdentityAssembler extends ResourceAssemblerSupport<Object, ResourceWrapper> {
	
	@Autowired
	private SelfLinkProvider linkProvider;

	public IdmIdentityAssembler() {
		super(IdmIdentityController.class, ResourceWrapper.class);
	}

	@Override
	public ResourceWrapper toResource(Object identity) {
		ResourceWrapper wrapper = instantiateResource(identity);
		wrapper.add(linkProvider.createSelfLinkFor(identity));
		return wrapper;
	}

	@Override
	protected ResourceWrapper instantiateResource(Object entity) {
		return new ResourceWrapper<Object>(entity);
	}

}
