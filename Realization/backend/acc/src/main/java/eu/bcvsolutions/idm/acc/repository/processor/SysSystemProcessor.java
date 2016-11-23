package eu.bcvsolutions.idm.acc.repository.processor;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.rest.impl.SysSystemController;
import eu.bcvsolutions.idm.acc.rest.lookup.SysSystemLookup;

@Component
public class SysSystemProcessor implements ResourceProcessor<Resource<SysSystem>> {

	@Autowired
	private SysSystemLookup systemLookup;
	
	@Override
	public Resource<SysSystem> process(Resource<SysSystem> resource) {
		String name = String.valueOf(systemLookup.getResourceIdentifier(resource.getContent()));
		resource.add(linkTo(methodOn(SysSystemController.class)
				.getConnectorFormDefinition(name, null)).withRel("connector-form-definition"));
		resource.add(linkTo(methodOn(SysSystemController.class)
				.getConnectorFormValues(name, null)).withRel("connector-form-values"));
		return resource;
	}

}
