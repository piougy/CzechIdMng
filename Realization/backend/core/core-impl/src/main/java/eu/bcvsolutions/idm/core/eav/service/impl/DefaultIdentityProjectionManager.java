package eu.bcvsolutions.idm.core.eav.service.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.FormableFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractPositionFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.projection.IdmIdentityProjectionDto;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.AutomaticRoleManager;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmContractPositionService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.dto.FormDefinitionAttributes;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormProjectionManager;
import eu.bcvsolutions.idm.core.eav.api.service.IdentityProjectionManager;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
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
 * @see FormProjectionManager
 */
public class DefaultIdentityProjectionManager implements IdentityProjectionManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdentityProjectionManager.class);
	//
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmIdentityContractService contractService;
	@Autowired private IdmContractPositionService contractPositionService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private LookupService lookupService;
	@Autowired private EntityEventManager entityEventManager;
	@Autowired private ObjectMapper mapper;
	
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
		// assigned roles - new identity only
		saveIdentityRoles(event, permission);
		//
		return event.getContent();
	}
	
	protected IdmIdentityDto saveIdentity(EntityEvent<IdmIdentityProjectionDto> event, BasePermission... permission) {
		IdentityEventType eventType = IdentityEventType.CREATE;
		IdmIdentityProjectionDto dto = event.getContent();
		IdmIdentityDto identity = dto.getIdentity();
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
		// if contract is saved with identity => automatic roles for identity has to be skipped, 
		// will be recount with contract together
		// check contract has to be saved
		IdmIdentityContractDto contract = dto.getContract();
		if (contract != null ) {
			if (identity.getFormProjection() == null) {
				LOG.debug("Automatic roles will be recount with contract together. Projection s not defined.");
				//
				identityEvent.getProperties().put(AutomaticRoleManager.SKIP_RECALCULATION, Boolean.TRUE);
			} else {
				IdmFormProjectionDto formProjection = lookupService.lookupEmbeddedDto(dto.getIdentity(), IdmIdentity_.formProjection);
				if (formProjection.getProperties().getBooleanValue(IdentityFormProjectionRoute.PARAMETER_ALL_CONTRACTS)
						|| formProjection.getProperties().getBooleanValue(IdentityFormProjectionRoute.PARAMETER_PRIME_CONTRACT)) {
					LOG.debug("Automatic roles will be recount with contract together. Projection [{}] saves contracts.", 
							formProjection.getCode());
					//
					identityEvent.getProperties().put(AutomaticRoleManager.SKIP_RECALCULATION, Boolean.TRUE);
				}
			}
		}
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
		//
		// check contract has to be saved
		if (identity.getFormProjection() != null) {
			IdmFormProjectionDto formProjection = lookupService.lookupEmbeddedDto(dto.getIdentity(), IdmIdentity_.formProjection);
			if (!formProjection.getProperties().getBooleanValue(IdentityFormProjectionRoute.PARAMETER_ALL_CONTRACTS)
					&& !formProjection.getProperties().getBooleanValue(IdentityFormProjectionRoute.PARAMETER_PRIME_CONTRACT)) {
				LOG.debug("Projection [{}] doesn't save prime contract.", formProjection.getCode());
				return contract;
			}
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
		// check all contracts has to be saved
		IdmIdentityDto identity = dto.getIdentity();
		if (identity.getFormProjection() != null) {
			IdmFormProjectionDto formProjection = lookupService.lookupEmbeddedDto(dto.getIdentity(), IdmIdentity_.formProjection);
			if (!formProjection.getProperties().getBooleanValue(IdentityFormProjectionRoute.PARAMETER_ALL_CONTRACTS)) {
				LOG.debug("Projection [{}] doesn't save other contracts.", formProjection.getCode());
				return savedContracts;
			}
		}
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
			boolean primeContractChanged = false;
			//
			for (IdmIdentityContractDto contract : previousProjection.getOtherContracts()) {
				if (Objects.equals(dto.getContract(), contract)) {
					// new prime contract is saved all time
					primeContractChanged = true; 
					continue;
				}
				//
				deleteContract(event, contract, permission);
			}
			// delete previous prime contract, if order of contracts changed
			if (primeContractChanged && !savedContracts.contains(previousProjection.getContract())) {
				deleteContract(event, previousProjection.getContract(), permission);
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
		// check other contract position has to be saved
		IdmIdentityDto identity = dto.getIdentity();
		if (identity.getFormProjection() != null) {
			IdmFormProjectionDto formProjection = lookupService.lookupEmbeddedDto(dto.getIdentity(), IdmIdentity_.formProjection);
			if (!formProjection.getProperties().getBooleanValue(IdentityFormProjectionRoute.PARAMETER_OTHER_POSITION)) {
				LOG.debug("Projection [{}] doesn't save other contract positions.", formProjection.getCode());
				return savedPositions;
			}
		}
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
				requestEvent.setParentId(event.getId());
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
		context.setAddPermissions(true);
		// load (~filter) specified form definitions and attributes only
		setAddEavMetadata(context, identity);
		// evaluate access / load eavs
		identity = identityService.get(identity, context, permission);
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
		IdmIdentityDto identity = dto.getIdentity();
		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setAddPermissions(true);
		contractFilter.setIdentity(identity.getId());
		setAddEavMetadata(contractFilter, identity);
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
		// check all assigned roles has to be loaded
		IdmIdentityDto identity = dto.getIdentity();
		if (identity.getFormProjection() != null) {
			IdmFormProjectionDto formProjection = lookupService.lookupEmbeddedDto(dto.getIdentity(), IdmIdentity_.formProjection);
			ConfigurationMap properties = formProjection.getProperties();
			//
			if (properties.containsKey(IdentityFormProjectionRoute.PARAMETER_LOAD_ASSIGNED_ROLES) // backward compatible
					&& !properties.getBooleanValue(IdentityFormProjectionRoute.PARAMETER_LOAD_ASSIGNED_ROLES)) {
				LOG.debug("Projection [{}] does not load all assigned roles.", formProjection.getCode());
				//
				return Lists.newArrayList();
			}
		}
		//
		// load all assigned identity roles
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(identity.getId());
		//
		return Lists.newArrayList(identityRoleService.find(filter, null, permission).getContent());
	}
	
	/**
	 * Load extended attributes, if needed by projection.
	 * 
	 * @param context
	 * @param identity
	 */
	protected void setAddEavMetadata(FormableFilter context, IdmIdentityDto identity) {
		if (identity.getFormProjection() == null) {
			// load all form instances => ~ full form projection as default, when no projection is specified
			context.setAddEavMetadata(Boolean.TRUE);
			return;
		}
		//
		IdmFormProjectionDto formProjection = lookupService.lookupEmbeddedDto(identity, IdmIdentity_.formProjection);
		String formDefinitions = formProjection.getFormDefinitions();
		if (StringUtils.isEmpty(formDefinitions)) {
			// form instances are not needed - not configured in this projection
			return;
		}
		//
		try {
			List<FormDefinitionAttributes> attributes = mapper.readValue(formDefinitions, new TypeReference<List<FormDefinitionAttributes>>() {});
			if (!attributes.isEmpty()) {
				context.setFormDefinitionAttributes(attributes);
			} else {
				LOG.debug("Extended attribute values is not needed by form projection [{}], will not be loaded.");
			}
		} catch (IOException ex) {
			LOG.warn("Form projection [{}] is wrongly configured. Fix configured form definitions. "
					+ "All eav attributes will be loaded as default.",
					formProjection.getCode(), ex);
			context.setAddEavMetadata(Boolean.TRUE);
		}
	}
	
	private void deleteContract(EntityEvent<IdmIdentityProjectionDto> event, IdmIdentityContractDto contract, BasePermission... permission) {
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
