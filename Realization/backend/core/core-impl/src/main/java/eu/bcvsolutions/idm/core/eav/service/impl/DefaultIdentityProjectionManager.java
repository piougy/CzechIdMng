package eu.bcvsolutions.idm.core.eav.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.config.domain.PrivateIdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractPositionFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.projection.IdmIdentityProjectionDto;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmContractPositionService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.service.IdentityProjectionManager;
import eu.bcvsolutions.idm.core.model.event.ContractPositionEvent;
import eu.bcvsolutions.idm.core.model.event.ContractPositionEvent.ContractPositionEventType;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;

/**
 * Identity projection - get / save.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
public class DefaultIdentityProjectionManager implements IdentityProjectionManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdentityProjectionManager.class);
	//
	@Autowired private IdmIdentityService identityService;
	@Autowired private PrivateIdentityConfiguration identityConfiguration;
	@Autowired private IdmIdentityContractService contractService;
	@Autowired private IdmContractPositionService contractPositionService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private LookupService lookupService;
	@Autowired private EntityEventManager entityEventManager;
	
	@Override
	public IdmIdentityProjectionDto get(Serializable codeableIdentifier, BasePermission... permission) {
		LOG.trace("Load identity projection [{}]", codeableIdentifier);
		IdmIdentityDto identity = getIdentity(codeableIdentifier, permission);
		IdmIdentityProjectionDto dto = new IdmIdentityProjectionDto(identity);
		// contracts
		List<IdmIdentityContractDto> contracts = getContracts(dto, permission);
		if (!contracts.isEmpty()) {
			IdmIdentityContractDto contract = contracts.get(0);
			// prime contract
			dto.setContract(contract);
			contracts.removeIf(c -> Objects.equals(c, contract));
			// other contracts
			dto.setOtherContracts(contracts);
		}
		// other positions
		dto.setOtherPositions(getOtherPositions(dto, permission));
		// assigned roles
		dto.setIdentityRoles(getIdentityRoles(dto, permission));
		//
		LOG.trace("Loaded identity projection [{}]", codeableIdentifier);
		return dto;
	}
	
	@Override
	@Transactional
	public EventContext<IdmIdentityProjectionDto> publish(EntityEvent<IdmIdentityProjectionDto> event, BasePermission... permission) {
		return publish(event, (EntityEvent<?>) null, permission);
	}
	
	@Override
	@Transactional
	public EventContext<IdmIdentityProjectionDto> publish(
			EntityEvent<IdmIdentityProjectionDto> event, 
			EntityEvent<?> parentEvent, 
			BasePermission... permission) {
		Assert.notNull(event, "Event must be not null!");
		IdmIdentityProjectionDto dto = event.getContent();
		Assert.notNull(dto, "Content (dto) in event must be not null!");
		// check permissions - check access to filled form values
		event.setPermission(permission);
		// load previous projection
		IdmIdentityDto identity = dto.getIdentity();
		if (!identityService.isNew(identity)) {
			event.setOriginalSource(
					get(identity.getId(), PermissionUtils.isEmpty(permission) ? null : IdmBasePermission.UPDATE) // UPDATE permission
			);
		}
		//
		return entityEventManager.process(event, parentEvent);
	}
	
	@Override
	@Transactional
	public IdmIdentityProjectionDto saveInternal(EntityEvent<IdmIdentityProjectionDto> event, BasePermission... permission) {
		Assert.notNull(event, "Event is required.");
		IdmIdentityProjectionDto dto = event.getContent();
		Assert.notNull(dto, "DTO is required.");
		// identity
		IdmIdentityDto identity = saveIdentity(event, permission);
		dto.setIdentity(identity);
		event.setContent(dto);
		// prime contract - will be saved all time
		dto.setContract(saveContract(event, permission));
		event.setContent(dto);
		// other contracts
		dto.setOtherContracts(saveOtherContracts(event, permission));
		event.setContent(dto);
		// other positions
		dto.setOtherPositions(saveOtherPositions(event, permission));
		event.setContent(dto);
		//
		// assigned roles - new identity only
		saveIdentityRoles(event, permission);
		//
		// reload all
		return event.getContent();
	}
	
	protected IdmIdentityDto saveIdentity(EntityEvent<IdmIdentityProjectionDto> event, BasePermission... permission) {
		IdentityEventType eventType = IdentityEventType.CREATE;
		IdmIdentityDto identity = event.getContent().getIdentity();
		IdmIdentityProjectionDto previousProjection = event.getOriginalSource();
		//
		if (previousProjection != null) {
			eventType = IdentityEventType.UPDATE;
			identity.setState(previousProjection.getIdentity().getState());
		} else {
			identity.setState(IdentityState.CREATED);
		}
		//
		EntityEvent<IdmIdentityDto> identityEvent = new IdentityEvent(eventType, identity);
		// disable default contract creation
		identityEvent.getProperties().put(IdmIdentityContractService.SKIP_CREATION_OF_DEFAULT_POSITION, Boolean.TRUE);
		// publish
		return identityService.publish(identityEvent, event, permission).getContent();
	}
	
	/**
	 * Save the first ~ prime contract.
	 * 
	 * @param event
	 * @param permission
	 * @return
	 */
	protected IdmIdentityContractDto saveContract(EntityEvent<IdmIdentityProjectionDto> event, BasePermission... permission) {
		IdmIdentityProjectionDto dto = event.getContent();
		IdmIdentityDto identity = dto.getIdentity();
		//
		IdmIdentityContractDto contract = dto.getContract();
		if (contract == null) {
			// prime contract was not sent => not save, but is needed in other processing
			List<IdmIdentityContractDto> contracts = getContracts(dto, PermissionUtils.isEmpty(permission) ? null : IdmBasePermission.READ);
			if (contracts.isEmpty()) {
				return null;
			}
			return contracts.get(0); // ~ prime contract
		}
		contract.setIdentity(identity.getId());
		IdentityContractEventType contractEventType = IdentityContractEventType.CREATE;
		if (!contractService.isNew(contract)) {
			contractEventType = IdentityContractEventType.UPDATE;
		}
		EntityEvent<IdmIdentityContractDto> contractEvent = new IdentityContractEvent(contractEventType, contract);
		//
		return contractService.publish(contractEvent, event, permission).getContent();
	}
	
	protected List<IdmIdentityContractDto> saveOtherContracts(EntityEvent<IdmIdentityProjectionDto> event, BasePermission... permission) {
		IdmIdentityProjectionDto dto = event.getContent();
		IdmIdentityProjectionDto previousProjection = event.getOriginalSource();
		List<IdmIdentityContractDto> savedContracts = new ArrayList<>(dto.getOtherContracts().size());
		//
		for (IdmIdentityContractDto contract : dto.getOtherContracts()) {			
			IdentityContractEventType contractEventType = IdentityContractEventType.CREATE;
			if (!contractService.isNew(contract)) {
				contractEventType = IdentityContractEventType.UPDATE;
				// TODO: validation - identity cannot be changed
			} else {
				contract.setIdentity(dto.getIdentity().getId());
			}
			IdentityContractEvent otherContractEvent = new IdentityContractEvent(contractEventType, contract);
			//
			savedContracts.add(contractService.publish(otherContractEvent, event, permission).getContent());
			if (previousProjection != null) {
				previousProjection.getOtherContracts().removeIf(c -> {
					return Objects.equals(c.getId(), contract.getId());
				});
			}
		}
		// remove not sent contracts, if previous exists
		if (previousProjection != null) {
			for (IdmIdentityContractDto contract : previousProjection.getOtherContracts()) {
				IdentityContractEventType contractEventType = IdentityContractEventType.DELETE;
				IdentityContractEvent otherContractEvent = new IdentityContractEvent(contractEventType, contract);
				//
				contractService.publish(
						otherContractEvent,
						event,
						PermissionUtils.isEmpty(permission) ? null : IdmBasePermission.DELETE
				);
			}
		}
		//
		return savedContracts;
	}
	
	protected List<IdmContractPositionDto> saveOtherPositions(
			EntityEvent<IdmIdentityProjectionDto> event, 
			BasePermission... permission) {
		IdmIdentityProjectionDto dto = event.getContent();
		IdmIdentityProjectionDto previousProjection = event.getOriginalSource();
		List<IdmContractPositionDto> savedPositions = new ArrayList<>(dto.getOtherPositions().size());
		IdmIdentityContractDto contract = dto.getContract();
		//
		for (IdmContractPositionDto otherPosition : dto.getOtherPositions()) {
			if (otherPosition.getIdentityContract() == null) {
				if (contract == null) {
					throw new ForbiddenEntityException("contract", IdmBasePermission.READ);
				}
				otherPosition.setIdentityContract(contract.getId());
			}
			ContractPositionEventType positionEventType = ContractPositionEventType.CREATE;
			if (!contractPositionService.isNew(otherPosition)) {
				positionEventType = ContractPositionEventType.UPDATE;
			}
			ContractPositionEvent positionEvent = new ContractPositionEvent(positionEventType, otherPosition);
			//
			savedPositions.add(contractPositionService.publish(positionEvent, event, permission).getContent());
			if (previousProjection != null) { 
				previousProjection.getOtherPositions().removeIf(p -> {
					return Objects.equals(p.getId(), otherPosition.getId());
				});
			}
		}
		// remove not sent positions, if previous exists 
		if (previousProjection != null) {
			for (IdmContractPositionDto contractPosition : previousProjection.getOtherPositions()) {
				ContractPositionEventType positionEventType = ContractPositionEventType.DELETE;
				ContractPositionEvent positionEvent = new ContractPositionEvent(positionEventType, contractPosition);
				//
				contractPositionService.publish(
						positionEvent, 
						event, 
						PermissionUtils.isEmpty(permission) ? null : IdmBasePermission.DELETE
				);
			}
		}
		//
		return savedPositions;
	}
	
	protected void saveIdentityRoles(EntityEvent<IdmIdentityProjectionDto> event, BasePermission... permission) {
		IdmIdentityProjectionDto dto = event.getContent();
		IdmIdentityProjectionDto previousProjection = event.getOriginalSource();
		IdmIdentityContractDto contract = dto.getContract();
		IdmIdentityDto identity = dto.getIdentity();
		//
		if (previousProjection == null) {
			List<IdmConceptRoleRequestDto> concepts = new ArrayList<>(dto.getIdentityRoles().size());
			//
			for (IdmIdentityRoleDto assignedRole : dto.getIdentityRoles()) {
				// create new identity role
				IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
				if (assignedRole.getIdentityContract() != null) {
					concept.setIdentityContract(assignedRole.getIdentityContract());
				} else if (contract != null) {
					concept.setIdentityContract(contract.getId());
				} else {
					throw new ForbiddenEntityException("contract", IdmBasePermission.READ);
				}
				concept.setRole(assignedRole.getRole());
				concept.setOperation(ConceptRoleRequestOperation.ADD);
				concept.setValidFrom(assignedRole.getValidFrom());
				concept.setValidTill(assignedRole.getValidTill());
				//
				concepts.add(concept);
			}
			if (!concepts.isEmpty()) {
				IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
				roleRequest.setState(RoleRequestState.CONCEPT);
				roleRequest.setExecuteImmediately(false);
				roleRequest.setApplicant(identity.getId());
				roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
				roleRequest = roleRequestService.save(roleRequest);
				//
				for (IdmConceptRoleRequestDto concept : concepts) {
					concept.setRoleRequest(roleRequest.getId());
					//
					conceptRoleRequestService.save(concept);
				}
				//
				// start event with skip check authorities
				RoleRequestEvent requestEvent = new RoleRequestEvent(RoleRequestEventType.EXCECUTE, roleRequest);
				requestEvent.getProperties().put(IdmIdentityRoleService.SKIP_CHECK_AUTHORITIES, Boolean.TRUE);
				requestEvent.setPriority(event.getPriority()); // frontend
				// prevent to start asynchronous event before previous update event is completed. 
				requestEvent.setSuperOwnerId(identity.getId());
				//
				roleRequestService.startRequestInternal(requestEvent);
			}
		}
	}
	
	/**
	 * Load identity.
	 * 
	 * @param codeableIdentifier  uuid or username
	 * @param permission
	 * @return
	 */
	protected IdmIdentityDto getIdentity(Serializable codeableIdentifier, BasePermission... permission) {
		// codeable decorator
		IdmIdentityDto identity = lookupService.lookupDto(IdmIdentityDto.class, codeableIdentifier);
		if (identity == null) {
			throw new EntityNotFoundException(identityService.getEntityClass(), codeableIdentifier);
		}
		//
		IdmIdentityFilter context = new IdmIdentityFilter();
		context.setAddEavMetadata(Boolean.TRUE);
		context.setAddPermissions(true);
		// evaluate access / load eavs
		identity = identityService.get(
				identity, 
				context, 
				identityConfiguration.isFormAttributesSecured() ? permission : null
		);
		if (!identityConfiguration.isFormAttributesSecured() && !PermissionUtils.isEmpty(permission)) {
			try {
				identityService.checkAccess(identity, IdmBasePermission.READ, IdmBasePermission.UPDATE);
			} catch (ForbiddenEntityException ex) {
				identity.getEavs().forEach(formInstance -> {
					formInstance.getFormDefinition().getFormAttributes().forEach(formAttribute -> {
						formAttribute.setReadonly(true);
					});
				});
			}
		}
		//
		return identity;
	}
	
	/**
	 * Load others than prime contract.
	 * 
	 * @param dto
	 * @param permission
	 * @return
	 */
	protected List<IdmIdentityContractDto> getContracts(IdmIdentityProjectionDto dto, BasePermission... permission) {
		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setAddEavMetadata(Boolean.TRUE);
		contractFilter.setAddPermissions(true);
		contractFilter.setIdentity(dto.getIdentity().getId());
		// eav attributes are secured automatically on this form (without configuration is needed)
		List<IdmIdentityContractDto> contracts = Lists.newArrayList(contractService
				.find(contractFilter, null, permission)
				.getContent());
		//
		// sort -> prime first 
		contractService.sortByPrimeContract(contracts);	
		//
		return contracts;
	}
	
	/**
	 * Load other positions.
	 * 
	 * @param dto
	 * @param permission
	 * @return
	 */
	protected List<IdmContractPositionDto> getOtherPositions(IdmIdentityProjectionDto dto, BasePermission... permission) {
		IdmContractPositionFilter positionFilter = new IdmContractPositionFilter();
		positionFilter.setIdentity(dto.getIdentity().getId());
		positionFilter.setAddPermissions(true);
		//
		return Lists.newArrayList(contractPositionService.find(positionFilter, null, permission).getContent());
	}
	
	/**
	 * Load assigned roles.
	 * 
	 * @param dto
	 * @param permission
	 * @return
	 */
	protected List<IdmIdentityRoleDto> getIdentityRoles(IdmIdentityProjectionDto dto, BasePermission... permission) {
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(dto.getIdentity().getId());
		//
		return Lists.newArrayList(identityRoleService.find(filter, null, permission).getContent());
	}
}
