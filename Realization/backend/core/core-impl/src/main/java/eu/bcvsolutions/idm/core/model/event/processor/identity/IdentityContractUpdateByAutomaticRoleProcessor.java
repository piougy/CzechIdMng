package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;

/**
 * Automatic roles recount while identity contract is saved, updated or deleted / disabled.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Automatic roles recount while identity contract is updated, disabled or enabled.")
public class IdentityContractUpdateByAutomaticRoleProcessor extends CoreEventProcessor<IdmIdentityContractDto> {
	
	public static final String PROCESSOR_NAME = "identity-contract-update-by-automatic-role-processor";
	@Autowired
	private IdmIdentityContractRepository repository;
	@Autowired
	private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	private ModelMapper modelMapper;
	
	public IdentityContractUpdateByAutomaticRoleProcessor() {
		super(IdentityContractEventType.UPDATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentityContractDto contract = event.getContent();
		final IdmIdentityContract contractEntity = new IdmIdentityContract();
		modelMapper.map(contract, contractEntity);
		if (contract.isDisabled()) {
			// Nothing to do - contract is disabled
			return new DefaultEventResult<>(event, this);
		}
		//
		// prepare request
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		roleRequest.setApplicant(contract.getIdentity());
		roleRequest.setRequestedByType(RoleRequestedByType.AUTOMATICALLY);
		roleRequest.setExecuteImmediately(true);
		// we cann't save immediately, request will be saved in method 'checkSavedRequest'
		//
		IdmIdentityContract previous = repository.getPersistedIdentityContract(contract.getId());
		UUID previousPosition = previous.getWorkPosition() == null ? null : previous.getWorkPosition().getId();
		UUID newPosition = contract.getWorkPosition();
		//
		// check if new and old work position are same
		// check automatic roles - if position or disabled was changed
		if (!Objects.equals(newPosition, previousPosition)
				|| (!contract.isDisabled() && previous.isDisabled() != contract.isDisabled())) {
			// work positions has some difference
			List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByContract(contract.getId());
			Set<IdmRoleTreeNodeDto> previousAutomaticRoles = assignedRoles.stream()
					.filter(identityRole -> {
						return identityRole.getRoleTreeNode() != null;
					}).map(identityRole -> {
						return roleTreeNodeService.get(identityRole.getRoleTreeNode());
					}).collect(Collectors.toSet());
			Set<IdmRoleTreeNodeDto> addedAutomaticRoles = new HashSet<>();
			if (newPosition != null) {
				addedAutomaticRoles = roleTreeNodeService.getAutomaticRolesByTreeNode(newPosition);
			}
			//
			Set<IdmRoleTreeNodeDto> removedAutomaticRoles = new HashSet<>(previousAutomaticRoles);
			removedAutomaticRoles.removeAll(addedAutomaticRoles);
			addedAutomaticRoles.removeAll(previousAutomaticRoles);
			//
			for(IdmRoleTreeNodeDto removedAutomaticRole : removedAutomaticRoles) {
				Iterator<IdmIdentityRoleDto> iter = assignedRoles.iterator();
				while (iter.hasNext()){
					IdmIdentityRoleDto identityRole = iter.next();				
					if (Objects.equals(identityRole.getRoleTreeNode(), removedAutomaticRole)) {					
						// check, if role will be added by new automatic roles and prevent removing
						IdmRoleTreeNodeDto addedAutomaticRole = getByRole(identityRole.getRole(), addedAutomaticRoles);
						if (addedAutomaticRole == null) {
							roleRequest = checkSavedRequest(roleRequest);
							createConcept(roleRequest, identityRole.getId(), contract, identityRole.getRole(), removedAutomaticRole.getId(), ConceptRoleRequestOperation.REMOVE);
							iter.remove();
						} else {
							// change relation only
							// TODO: identity relo dto
							identityRole.setRoleTreeNode(addedAutomaticRole.getId());
							identityRoleService.save(identityRole);
							// new automatic role is not needed
							addedAutomaticRoles.remove(addedAutomaticRole);
						}
					}
			    }
			}
			//
			// change date - for unchanged assigned roles only
			if (EntityUtils.validableChanged(previous, contractEntity)) {
				roleRequest = checkSavedRequest(roleRequest);
				changeValidable(contract, assignedRoles, roleRequest);
			}
			
			for (IdmRoleTreeNodeDto addedAutomaticRole : addedAutomaticRoles) {
				roleRequest = checkSavedRequest(roleRequest);
				createConcept(roleRequest, null, contract, addedAutomaticRole.getRole(), addedAutomaticRole.getId(), ConceptRoleRequestOperation.ADD);
			}
		}
		//
		// process validable change
		else if (EntityUtils.validableChanged(previous, contractEntity)) {
			roleRequest = checkSavedRequest(roleRequest);
			changeValidable(contract, identityRoleService.findAllByContract(contract.getId()), roleRequest);
		}
		//
		// role request may not exist
		if (roleRequest.getId() != null) {
			roleRequestService.startRequestInternal(roleRequest.getId(), false);
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Method check if role request is saved, when request isn't saved save them.
	 * 
	 * @param roleRequest
	 * @return
	 */
	private IdmRoleRequestDto checkSavedRequest(IdmRoleRequestDto roleRequest) {
		if (roleRequest.getId() == null) {
			roleRequest = this.roleRequestService.save(roleRequest);
		}
		return roleRequest;
	}
	
	/**
	 * Method create {@link IdmConceptRoleRequestDto}
	 * @param roleRequest
	 * @param contract
	 * @param roleId
	 * @param roleTreeNodeId
	 * @param operation
	 * @return
	 */
	private IdmConceptRoleRequestDto createConcept(IdmRoleRequestDto roleRequest, UUID identityRoleId,
			IdmIdentityContractDto contract, UUID roleId, UUID roleTreeNodeId,
			ConceptRoleRequestOperation operation) {
		IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
		conceptRoleRequest.setRoleRequest(roleRequest.getId());
		conceptRoleRequest.setIdentityContract(contract.getId());
		conceptRoleRequest.setValidFrom(contract.getValidFrom());
		conceptRoleRequest.setValidTill(contract.getValidTill());
		// identity role can be null (CREATE)
		conceptRoleRequest.setIdentityRole(identityRoleId);
		//
		conceptRoleRequest.setRole(roleId);
		conceptRoleRequest.setRoleTreeNode(roleTreeNodeId);
		conceptRoleRequest.setOperation(operation);
		return conceptRoleRequestService.save(conceptRoleRequest);
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
	 * 
	 * @param contract
	 * @param assignedRoles
	 */
	private void changeValidable(IdmIdentityContractDto contract, List<IdmIdentityRoleDto> assignedRoles, IdmRoleRequestDto roleRequest) {
		assignedRoles.stream()
		.filter(identityRole -> {
			// TODO: automatic roles only?
			return identityRole.getRoleTreeNode() != null;
		}).forEach(identityRole -> {
			createConcept(roleRequest, identityRole.getId(), contract, identityRole.getRole(), identityRole.getRoleTreeNode(), ConceptRoleRequestOperation.UPDATE);
		});
	}
	
	/**
	 * before save - wee need to check changes - see detach in process method
	 */
	@Override
	public int getOrder() {
		return super.getOrder() - 1;
	}

}
