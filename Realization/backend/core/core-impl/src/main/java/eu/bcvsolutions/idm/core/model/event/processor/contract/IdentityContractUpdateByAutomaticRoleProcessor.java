package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractPositionFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.AutomaticRoleManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractPositionService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;

/**
 * Automatic roles by tree structure recount while identity contract is saved, updated or deleted / disabled.
 * 
 * @author Radek Tomiška
 * @author Ondřej Kopr
 *
 */
@Component(IdentityContractUpdateByAutomaticRoleProcessor.PROCESSOR_NAME)
@Description("Automatic roles by tree structure recount while identity contract is updated, disabled or enabled.")
public class IdentityContractUpdateByAutomaticRoleProcessor
		extends CoreEventProcessor<IdmIdentityContractDto> 
		implements IdentityContractProcessor {
	
	public static final String PROCESSOR_NAME = "identity-contract-update-by-automatic-role-processor";
	public static final String EVENT_PROPERTY_REQUEST = "idm:prepare-role-request";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityContractUpdateByAutomaticRoleProcessor.class);
	//
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmContractPositionService contractPositionService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private EntityStateManager entityStateManager;
	
	public IdentityContractUpdateByAutomaticRoleProcessor() {
		super(IdentityContractEventType.NOTIFY);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public boolean conditional(EntityEvent<IdmIdentityContractDto> event) {
		return super.conditional(event) 
				&& IdentityContractEventType.UPDATE.name().equals(event.getParentType()); 
	}

	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentityContractDto contract = event.getContent();
		IdmIdentityContractDto previous = event.getOriginalSource();
		UUID previousPosition = previous == null ? null : previous.getWorkPosition();
		UUID newPosition = contract.getWorkPosition();
		boolean validityChangedToValid = previous == null ? false : contract.isValidNowOrInFuture() && previous.isValidNowOrInFuture() != contract.isValidNowOrInFuture();
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		//
		// when automatic role recalculation is skipped, then flag for contract position is created only
		// flag can be processed afterwards
		if (getBooleanProperty(AutomaticRoleManager.SKIP_RECALCULATION, event.getProperties())) {
			LOG.debug("Automatic roles are skipped for contract [{}], state [{}] "
					+ "for position will be created only.", contract.getId(), CoreResultCode.AUTOMATIC_ROLE_SKIPPED.getCode());
			// 
			Map<String, Serializable> properties = new HashMap<>();
			// original contract as property
			properties.put(EntityEvent.EVENT_PROPERTY_ORIGINAL_SOURCE, event.getOriginalSource());
			entityStateManager.createState(
					contract, 
					OperationState.BLOCKED, 
					contract.isValidNowOrInFuture()
					?
					CoreResultCode.AUTOMATIC_ROLE_SKIPPED 
					: 
					CoreResultCode.AUTOMATIC_ROLE_SKIPPED_INVALID_CONTRACT, 
					properties
			);
			//
			return new DefaultEventResult<>(event, this);
		}
		if (!contract.isValidNowOrInFuture()) {
			// invalid contracts cannot have roles (roles for disabled contracts are removed by different processor or LRT)
			// but we need to add skipped flag above, even when invalid contract is updated
			return new DefaultEventResult<>(event, this);
		}
		//
		if (previous == null || !Objects.equals(newPosition, previousPosition) || validityChangedToValid) {
			// work positions has some difference or validity changes
			List<IdmIdentityRoleDto> assignedRoles = getAssignedAutomaticRoles(contract.getId());
			//
			// remove all automatic roles by attribute and by other contract position
			if (!assignedRoles.isEmpty()) {
				assignedRoles = assignedRoles
						.stream()
						.filter(autoRole -> {
							// remove automatic roles by attribute - solved by different process
							AbstractIdmAutomaticRoleDto automaticRoleDto = DtoUtils.getEmbedded(autoRole, IdmIdentityRole_.automaticRole, (AbstractIdmAutomaticRoleDto) null);
							if (automaticRoleDto instanceof IdmRoleTreeNodeDto) {
								return true;
							}
							return false;
						})
						.filter(identityRole -> {
							// remove automatic roles by attribute - solved by different process
							return identityRole.getContractPosition() == null;
						})
						.collect(Collectors.toList());
			}
			//
			Set<UUID> previousAutomaticRoles = assignedRoles
					.stream()
					.filter(identityRole -> {
						return identityRole.getAutomaticRole() != null;
					})
					.map(identityRole -> {
						return identityRole.getAutomaticRole();
					})
					.collect(Collectors.toSet());
			Set<IdmRoleTreeNodeDto> addedAutomaticRoles = new HashSet<>();
			if (newPosition != null && contract.isValidNowOrInFuture()) {
				addedAutomaticRoles = roleTreeNodeService.getAutomaticRolesByTreeNode(newPosition);
			}
			// prevent to remove newly added or still exists roles
			Set<UUID> removedAutomaticRoles = new HashSet<>(previousAutomaticRoles);
			removedAutomaticRoles.removeAll(addedAutomaticRoles
					.stream()
					.map(IdmRoleTreeNodeDto::getId)
					.collect(Collectors.toList())
					);
			addedAutomaticRoles.removeIf(a -> {
				return previousAutomaticRoles.contains(a.getId());
			});
			//
			for (UUID removedAutomaticRole : removedAutomaticRoles) {
				Iterator<IdmIdentityRoleDto> iter = assignedRoles.iterator();
				while (iter.hasNext()){
					IdmIdentityRoleDto identityRole = iter.next();				
					if (Objects.equals(identityRole.getAutomaticRole(), removedAutomaticRole)) {					
						// check, if role will be added by new automatic roles and prevent removing
						IdmRoleTreeNodeDto addedAutomaticRole = getByRole(identityRole.getRole(), addedAutomaticRoles);
						if (addedAutomaticRole == null) {
							// remove assigned role
							IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
							conceptRoleRequest.setIdentityRole(identityRole.getId());
							conceptRoleRequest.setRole(identityRole.getRole());
							conceptRoleRequest.setOperation(ConceptRoleRequestOperation.REMOVE);
							//
							roleRequest.getConceptRoles().add(conceptRoleRequest);
							
							iter.remove();
						} else {
							// change relation only
							IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
							conceptRoleRequest.setIdentityRole(identityRole.getId());
							conceptRoleRequest.setAutomaticRole(addedAutomaticRole.getId());
							conceptRoleRequest.setIdentityContract(contract.getId());
							conceptRoleRequest.setValidFrom(contract.getValidFrom());
							conceptRoleRequest.setValidTill(contract.getValidTill());
							conceptRoleRequest.setRole(identityRole.getRole());
							conceptRoleRequest.setOperation(ConceptRoleRequestOperation.UPDATE);
							//
							roleRequest.getConceptRoles().add(conceptRoleRequest);
							//
							// new automatic role is not needed
							addedAutomaticRoles.remove(addedAutomaticRole);
						}
					}
			    }
			}
			//
			// change date - for unchanged assigned roles only
			if (previous != null && EntityUtils.validableChanged(previous, contract)) {
				roleRequest.getConceptRoles().addAll(changeValidable(contract, assignedRoles));
			}
			//
			// add identity roles
			for (AbstractIdmAutomaticRoleDto autoRole : addedAutomaticRoles) {
				IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
				conceptRoleRequest.setIdentityContract(contract.getId());
				conceptRoleRequest.setValidFrom(contract.getValidFrom());
				conceptRoleRequest.setValidTill(contract.getValidTill());
				conceptRoleRequest.setRole(autoRole.getRole());
				conceptRoleRequest.setAutomaticRole(autoRole.getId());
				conceptRoleRequest.setOperation(ConceptRoleRequestOperation.ADD);
				//
				roleRequest.getConceptRoles().add(conceptRoleRequest);
			}
			//
			// contract is enabled => process all contract positions
			if (validityChangedToValid) {
				IdmContractPositionFilter filter = new IdmContractPositionFilter();
				filter.setIdentityContractId(contract.getId());
				//
				for (IdmContractPositionDto position : contractPositionService.find(filter, null).getContent()) {	
					CoreEvent<IdmContractPositionDto> positionEvent = new CoreEvent<>(CoreEventType.NOTIFY, position);
					positionEvent.setPriority(PriorityType.IMMEDIATE); // we don't need the second asynchronicity
					positionEvent.getProperties().put(EVENT_PROPERTY_REQUEST, roleRequest);
					// recount automatic roles for given position
					EventContext<IdmContractPositionDto> context = contractPositionService.publish(positionEvent, event);
					// get modified prepared request
					if (context.getLastResult() != null) {
						roleRequest = (IdmRoleRequestDto) context.getLastResult().getEvent().getProperties().get(EVENT_PROPERTY_REQUEST);
					}
				}
			}			
		} else if (previous != null && EntityUtils.validableChanged(previous, contract)) {
			// process validable change only
			roleRequest.getConceptRoles().addAll(changeValidable(contract, getAssignedAutomaticRoles(contract.getId())));
		}
		// start request at end asynchronously
		roleRequest.setApplicant(contract.getIdentity());
		RoleRequestEvent requestEvent = new RoleRequestEvent(RoleRequestEventType.EXCECUTE, roleRequest);
		roleRequestService.startConcepts(requestEvent, event);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	private IdmRoleTreeNodeDto getByRole(UUID roleId, Set<IdmRoleTreeNodeDto> automaticRoles) {
		for (IdmRoleTreeNodeDto automaticRole : automaticRoles) {
			if (automaticRole.getRole().equals(roleId)) {
				return automaticRole;
			}
		}
		return null;
	}
	
	private List<IdmIdentityRoleDto> getAssignedAutomaticRoles(UUID contractId) {
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityContractId(contractId);
		filter.setAutomaticRole(Boolean.TRUE);
		//
		return identityRoleService.find(filter, null).getContent();
	}
	
	/**
	 * Change dates for roles assigned by given contract (roles assigned by contract positions are included)
	 * 
	 * @param contract
	 * @param assignedRoles
	 */
	private List<IdmConceptRoleRequestDto> changeValidable(IdmIdentityContractDto contract, List<IdmIdentityRoleDto> assignedRoles) {
		List<IdmConceptRoleRequestDto> concepts = new ArrayList<>();
		if (assignedRoles.isEmpty()) {
			return concepts;
		}
		//
		assignedRoles
			.stream()
			.filter(identityRole -> {
				// automatic roles only
				return identityRole.getAutomaticRole() != null;
			})
			.forEach(identityRole -> {
				IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
				conceptRoleRequest.setIdentityRole(identityRole.getId());
				conceptRoleRequest.setAutomaticRole(identityRole.getAutomaticRole());
				conceptRoleRequest.setIdentityContract(contract.getId());
				conceptRoleRequest.setValidFrom(contract.getValidFrom());
				conceptRoleRequest.setValidTill(contract.getValidTill());
				conceptRoleRequest.setRole(identityRole.getRole());
				conceptRoleRequest.setOperation(ConceptRoleRequestOperation.UPDATE);
				conceptRoleRequest.setState(RoleRequestState.CONCEPT);
				//
				concepts.add(conceptRoleRequest);
			});
		//
		return concepts;
	}
	
	/**
	 * after save
	 */
	@Override
	public int getOrder() {
		return super.getOrder() + 500;
	}

}
