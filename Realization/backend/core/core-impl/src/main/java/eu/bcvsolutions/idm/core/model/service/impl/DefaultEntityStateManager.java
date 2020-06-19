package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

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
	@Transactional
	public IdmEntityStateDto saveState(
			Identifiable owner,
			IdmEntityStateDto state,
			BasePermission... permission) {
		Assert.notNull(state, "State is required.");
		//
		if (state.getOwnerId() == null) {
			state.setOwnerId(lookupService.getOwnerId(owner));
		}
		if (state.getOwnerType() == null) {
			Assert.notNull(owner, "Owner is required.");
			state.setOwnerType(getOwnerType(owner));
		}
		if (state.getInstanceId() == null) {
			state.setInstanceId(configurationService.getInstanceId());
		}
		//
		return entityStateService.save(state, permission);
	}
	
	@Override
	@Transactional
	public IdmEntityStateDto createState(
			Identifiable owner,
			OperationState operationState,
			ResultCode code,
			Map<String, Serializable> properties,
			BasePermission... permission) {
		IdmEntityStateDto state = new IdmEntityStateDto();
		
		Map<String, Object> modelParameters = null;
		if (properties != null) {
			modelParameters = new HashMap<String, Object>(properties);
		}
		
		state.setResult(
				new OperationResultDto
					.Builder(operationState == null ? OperationState.CREATED : operationState)
					.setModel(code == null ? null : new DefaultResultModel(code, modelParameters))
					.build());
		//
		return saveState(owner, state, permission);
	}
	
	@Override
	public Page<IdmEntityStateDto> findStates(Identifiable owner, Pageable pageable, BasePermission... permission) {
		IdmEntityStateFilter filter = new IdmEntityStateFilter();
		filter.setOwnerId(lookupService.getOwnerId(owner));
		filter.setOwnerType(getOwnerType(owner));
		//
		return findStates(filter, pageable, permission);
	}

	@Override
	public Page<IdmEntityStateDto> findStates(IdmEntityStateFilter filter, Pageable pageable, BasePermission... permission) {
		return entityStateService.find(filter, pageable, permission);
	}

	@Override
	@Transactional
	public void deleteState(IdmEntityStateDto state, BasePermission... permission) {
		entityStateService.delete(state, permission);
	}
	
	@Override
	@Transactional
	public void deleteStates(Identifiable owner, OperationState operationState, ResultCode code, BasePermission... permission) {
		UUID ownerId = lookupService.getOwnerId(owner);
		Assert.notNull(ownerId, "State owner identifier is required.");
		//
		IdmEntityStateFilter filter = new IdmEntityStateFilter();
		filter.setOwnerId(ownerId);
		filter.setOwnerType(getOwnerType(owner));
		if (operationState != null) {
			filter.setStates(Lists.newArrayList(operationState));
		}
		if (code != null) {
			filter.setResultCode(code.getCode());
		}	
		//
		entityStateService.find(filter, null, permission).forEach(state -> {
			entityStateService.delete(state, permission);
		});
	}
}
