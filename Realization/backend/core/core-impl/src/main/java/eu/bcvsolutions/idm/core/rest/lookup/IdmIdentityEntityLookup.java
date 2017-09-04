package eu.bcvsolutions.idm.core.rest.lookup;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.rest.lookup.AbstractEntityLookup;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;

/**
 * Identity entity lookup by id or username (codeable)
 * 
 * @author Radek Tomiška
 */
@Component
public class IdmIdentityEntityLookup extends AbstractEntityLookup<IdmIdentity> {

	@Autowired private IdmIdentityRepository identityRepository;
	
	@Override
	public Serializable getIdentifier(IdmIdentity identity) {
		return identity.getCode();
	}

	@Override
	public IdmIdentity lookup(Serializable id) {
		IdmIdentity entity = null;
		try {
			entity = identityRepository.findOne(EntityUtils.toUuid(id));
		} catch (ClassCastException ex) {
			// simply not found
		}
		if (entity == null) {
			entity = identityRepository.findOneByUsername(id.toString());
		}
		return entity;
	}
}
