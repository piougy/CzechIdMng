package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.api.service.LookupService;

/**
 * Entity states
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
public class DefaultEntityStateManager implements EntityStateManager {

	@Autowired private IdmEntityStateService entityStateService;
	@Autowired private LookupService lookupService;
	@Autowired private ConfigurationService configurationService;
	
	@Override
	public String getOwnerType(Identifiable owner) {
		return lookupService.getOwnerType(owner);
	}

	@Override
	public String getOwnerType(Class<? extends Identifiable> ownerType) {
		return lookupService.getOwnerType(ownerType);
	}	

	@Override
	public IdmEntityStateDto saveState(Identifiable owner, IdmEntityStateDto state) {
		Assert.notNull(state);
		//
		if (state.getOwnerId() == null) {
			state.setOwnerId(lookupService.getOwnerId(owner));
		}
		if (state.getOwnerType() == null) {
			Assert.notNull(owner);
			state.setOwnerType(getOwnerType(owner));
		}
		if (state.getInstanceId() == null) {
			state.setInstanceId(configurationService.getInstanceId());
		}
		//
		return entityStateService.save(state);
	}
	
	@Override
	public Page<IdmEntityStateDto> findStates(Identifiable owner, Pageable pageable) {
		IdmEntityStateFilter filter = new IdmEntityStateFilter();
		filter.setOwnerId(lookupService.getOwnerId(owner));
		filter.setOwnerType(getOwnerType(owner));
		//
		return findStates(filter, pageable);
	}

	@Override
	public Page<IdmEntityStateDto> findStates(IdmEntityStateFilter filter, Pageable pageable) {
		return entityStateService.find(filter, pageable);
	}

	@Override
	public void deleteState(IdmEntityStateDto state) {
		entityStateService.delete(state);
	}
}
