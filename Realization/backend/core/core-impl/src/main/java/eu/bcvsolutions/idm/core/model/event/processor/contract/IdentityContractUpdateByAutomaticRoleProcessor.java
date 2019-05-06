package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
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
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmContractPositionService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;

/**
 * Automatic roles recount while identity contract is saved, updated or deleted / disabled.
 * 
 * @author Radek Tomiška
 * @author Ondřej Kopr
 *
 */
@Component
@Description("Automatic roles recount while identity contract is updated, disabled or enabled.")
public class IdentityContractUpdateByAutomaticRoleProcessor
		extends CoreEventProcessor<IdmIdentityContractDto> 
		implements IdentityContractProcessor {
	
	public static final String PROCESSOR_NAME = "identity-contract-update-by-automatic-role-processor";
	public static final String EVENT_PROPERTY_REQUEST = "idm:prepare-role-request";
	//
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmContractPositionService contractPositionService;
	@Autowired private IdmRoleRequestService roleRequestService;
	
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
		//
		IdmIdentityContractDto previous = event.getOriginalSource();
		UUID previousPosition = previous.getWorkPosition();
		UUID newPosition = contract.getWorkPosition();
		//
		// prepare empty request, will be propagate for preparing concepts
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		//
		// check if new and old work position are same
		// check automatic roles - if position or disabled was changed
		boolean validityChangedToValid = contract.isValidNowOrInFuture() && previous.isValidNowOrInFuture() != contract.isValidNowOrInFuture();
		if (!Objects.equals(newPosition, previousPosition) || validityChangedToValid) {
			// work positions has some difference or validity changes
			List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByContract(contract.getId());
			//
			// remove all automatic roles by attribute
			if (!assignedRoles.isEmpty()) {
				assignedRoles = assignedRoles
						.stream()
						.filter(autoRole -> {
							AbstractIdmAutomaticRoleDto automaticRoleDto = DtoUtils.getEmbedded(autoRole, IdmIdentityRole_.automaticRole, (AbstractIdmAutomaticRoleDto) null);
							if (automaticRoleDto instanceof IdmRoleTreeNodeDto) {
								return true;
							}
							return false;
						})
						.filter(identityRole -> {
							return identityRole.getContractPosition() == null;
						})
						.collect(Collectors.toList());
			}
			//
			Set<UUID> previousAutomaticRoles = assignedRoles.stream()
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
			for(UUID removedAutomaticRole : removedAutomaticRoles) {
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
			if (EntityUtils.validableChanged(previous, contract)) {
				changeValidable(contract, assignedRoles);
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
		}
		//
		// process validable change
		else if (EntityUtils.validableChanged(previous, contract)) {
			roleRequest.getConceptRoles().addAll(changeValidable(contract, identityRoleService.findAllByContract(contract.getId())));
		}
		// start request at end
		if (!roleRequest.getConceptRoles().isEmpty()) {
			roleRequestService.executeConceptsImmediate(contract.getIdentity(), roleRequest.getConceptRoles());
		}
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
