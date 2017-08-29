package eu.bcvsolutions.idm.core.rest.lookup;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.annotations.Beta;

import eu.bcvsolutions.idm.core.api.rest.lookup.AbstractEntityLookup;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;

/**
 * TODO: Codeable repository? Or add public methods to dtoService?
 * 
 * @author Radek Tomiška
 *
 */
@Beta
@Component
public class IdmRoleEntityLookup extends AbstractEntityLookup<IdmRole> {

	@Autowired private IdmRoleRepository roleRepository;
	
	@Override
	public Serializable getIdentifier(IdmRole entity) {
		return entity.getCode();
	}

	@Override
	public IdmRole lookup(Serializable id) {
		IdmRole entity = null;
		try {
			entity = roleRepository.findOne(EntityUtils.toUuid(id));
		} catch (ClassCastException ex) {
			// simply not found
		}
		if (entity == null) {
			entity = roleRepository.findOneByCode(id.toString());
		}
		return entity;
	}	
}
