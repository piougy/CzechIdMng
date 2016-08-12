package eu.bcvsolutions.idm.core.model.repository;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.support.EntityLookupSupport;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

@Component
public class IdmIdentityLookup extends EntityLookupSupport<IdmIdentity> {
	
	@Autowired
	private IdmIdentityRepository identityRepository;
	
	@Override
	public Serializable getResourceIdentifier(IdmIdentity identity) {
		return identity.getUsername();
	}

	/**
	 * Identity could be found by username (primary) or id
	 */
	@Override
	public Object lookupEntity(Serializable id) {
		IdmIdentity identity = identityRepository.findOneByUsername(id.toString());
		if(identity == null) {
			try {
				identity = identityRepository.findOne(Long.valueOf(id.toString()));
			} catch (NumberFormatException ex) {
				// simply not found		
			}
		}
		return identity;
	}
}
