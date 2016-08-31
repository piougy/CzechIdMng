package eu.bcvsolutions.idm.core.model.repository;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.support.EntityLookupSupport;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmOrganization;

/**
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
public class IdmOrganizationLookup extends EntityLookupSupport<IdmOrganization> {
	
	@Autowired
	private IdmOrganizationRepository organizationRepository;
	
	@Override
	public Serializable getResourceIdentifier(IdmOrganization organization) {
		return organization.getId();
	}

	@Override
	public Object lookupEntity(Serializable id) {
		return organizationRepository.findOne(Long.parseLong(id.toString()));
	}
	
	
}
