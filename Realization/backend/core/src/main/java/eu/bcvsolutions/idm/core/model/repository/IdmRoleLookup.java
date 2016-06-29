package eu.bcvsolutions.idm.core.model.repository;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.support.EntityLookupSupport;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmRole;

@Component
public class IdmRoleLookup extends EntityLookupSupport<IdmRole>{

	//private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdmRoleLookup.class);
	
	@Autowired
	private IdmRoleRepository roleRepository;
	
	@Override
	public Serializable getResourceIdentifier(IdmRole role) {
		return role.getName();
	}

	/**
	 * Role could be found by name (primary) or id
	 */
	@Override
	public Object lookupEntity(Serializable id) {
		IdmRole role = roleRepository.findOneByName(id.toString());
		if(role == null) {
			try {
				role = roleRepository.findOne(Long.valueOf(id.toString()));
			} catch (NumberFormatException ex) {
				// simply not found		
			}
		}
		return role;
	}
	

}
