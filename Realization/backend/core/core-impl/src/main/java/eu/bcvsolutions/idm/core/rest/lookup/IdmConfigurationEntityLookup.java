package eu.bcvsolutions.idm.core.rest.lookup;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.rest.lookup.AbstractEntityLookup;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.model.repository.IdmConfigurationRepository;

/**
 * Configuration item entity lookup
 * 
 * @author Radek Tomiška
 */
@Component
public class IdmConfigurationEntityLookup extends AbstractEntityLookup<IdmConfiguration> {

	@Autowired private IdmConfigurationRepository repository;
	
	@Override
	public Serializable getIdentifier(IdmConfiguration entity) {
		return entity.getCode();
	}

	@Override
	public IdmConfiguration lookup(Serializable id) {
		IdmConfiguration entity = null;
		try {
			entity = repository.findOne(EntityUtils.toUuid(id));
		} catch (ClassCastException ex) {
			// simply not found
		}
		if (entity == null) {
			entity = repository.findOneByName(id.toString());
		}
		return entity;
	}
	
	
}
