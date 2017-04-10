package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.model.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.model.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
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
public class IdentityContractUpdateByAutomaticRoleProcessor extends CoreEventProcessor<IdmIdentityContract> {
	
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
	
	public IdentityContractUpdateByAutomaticRoleProcessor() {
		super(IdentityContractEventType.UPDATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityContract> process(EntityEvent<IdmIdentityContract> event) {
		IdmIdentityContract contract = event.getContent();
		if (contract.isDisabled()) {
			// Nothing to do - contract is disabled
			return new DefaultEventResult<>(event, this);
		}
		//
		// prepare request
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		roleRequest.setApplicant(contract.getIdentity().getId());
		roleRequest.setRequestedByType(RoleRequestedByType.AUTOMATICALLY);
		roleRequest.setExecuteImmediately(true);
		// we cann't save immediately, request will be saved in method 'checkSavedRequest'
		//
		IdmIdentityContract previous = repository.getPersistedIdentityContract(contract);
		IdmTreeNode newPosition = contract.getWorkPosition();
		//
		// check if new and old work position are same
		// check automatic roles - if position or disabled was changed
		if (!Objects.equals(newPosition, previous.getWorkPosition())
				|| (!contract.isDisabled() && previous.isDisabled() != contract.isDisabled())) {
			// work positions has some difference
			List<IdmIdentityRole> assignedRoles = identityRoleService.getRoles(contract);
			Set<IdmRoleTreeNode> previousAutomaticRoles = assignedRoles.stream()
					.filter(identityRole -> {
						return identityRole.getRoleTreeNode() != null;
					}).map(identityRole -> {
						return identityRole.getRoleTreeNode();
					}).collect(Collectors.toSet());
			Set<IdmRoleTreeNode> addedAutomaticRoles = new HashSet<>();
			if (newPosition != null) {
				addedAutomaticRoles = roleTreeNodeService.getAutomaticRoles(newPosition);
			}
			//
			Set<IdmRoleTreeNode> removedAutomaticRoles = new HashSet<>(previousAutomaticRoles);
			removedAutomaticRoles.removeAll(addedAutomaticRoles);
			addedAutomaticRoles.removeAll(previousAutomaticRoles);
			//
			for(IdmRoleTreeNode removedAutomaticRole : removedAutomaticRoles) {
				Iterator<IdmIdentityRole> iter = assignedRoles.iterator();
				while (iter.hasNext()){
					IdmIdentityRole identityRole = iter.next();				
					if (Objects.equals(identityRole.getRoleTreeNode(), removedAutomaticRole)) {					
						// check, if role will be added by new automatic roles and prevent removing
						IdmRoleTreeNode addedAutomaticRole = getByRole(identityRole.getRole(), addedAutomaticRoles);
						if (addedAutomaticRole == null) {
							roleRequest = checkSavedRequest(roleRequest);
							createConcept(roleRequest, identityRole, contract, identityRole.getRole(), removedAutomaticRole, ConceptRoleRequestOperation.REMOVE);
							iter.remove();
						} else {
							// change relation only
							identityRole.setRoleTreeNode(addedAutomaticRole);
							identityRoleService.save(identityRole);
							// new automatic role is not needed
							addedAutomaticRoles.remove(addedAutomaticRole);
						}
					}
			    }
			}
			//
			// change date - for unchanged assigned roles only
			if (EntityUtils.validableChanged(previous, contract)) {
				roleRequest = checkSavedRequest(roleRequest);
				changeValidable(contract, assignedRoles, roleRequest);
			}
			
			for (IdmRoleTreeNode addedAutomaticRole : addedAutomaticRoles) {
				roleRequest = checkSavedRequest(roleRequest);
				createConcept(roleRequest, null, contract, addedAutomaticRole.getRole(), addedAutomaticRole, ConceptRoleRequestOperation.ADD);
			}
		}
		//
		// process validable change
		else if (EntityUtils.validableChanged(previous, contract)) {
			roleRequest = checkSavedRequest(roleRequest);
			changeValidable(contract, identityRoleService.getRoles(contract), roleRequest);
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
	 * @param role
	 * @param roleTreeNode
	 * @param operation
	 * @return
	 */
	private IdmConceptRoleRequestDto createConcept(IdmRoleRequestDto roleRequest, IdmIdentityRole identityRole,
			IdmIdentityContract contract, IdmRole role, IdmRoleTreeNode roleTreeNode,
			ConceptRoleRequestOperation operation) {
		IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
		conceptRoleRequest.setRoleRequest(roleRequest.getId());
		conceptRoleRequest.setIdentityContract(contract.getId());
		conceptRoleRequest.setValidFrom(contract.getValidFrom());
		conceptRoleRequest.setValidTill(contract.getValidTill());
		//
		// identity role can be null (CREATE)
		if (identityRole != null) {
			conceptRoleRequest.setIdentityRole(identityRole.getId());
		}
		//
		conceptRoleRequest.setRole(role.getId());
		conceptRoleRequest.setRoleTreeNode(roleTreeNode.getId());
		conceptRoleRequest.setOperation(operation);
		return conceptRoleRequestService.save(conceptRoleRequest);
	}
	
	private IdmRoleTreeNode getByRole(IdmRole role, Set<IdmRoleTreeNode> automaticRoles) {
		for (IdmRoleTreeNode automaticRole : automaticRoles) {
			if (automaticRole.getRole().equals(role)) {
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
	private void changeValidable(IdmIdentityContract contract, List<IdmIdentityRole> assignedRoles, IdmRoleRequestDto roleRequest) {
		assignedRoles.stream()
		.filter(identityRole -> {
			// TODO: automatic roles only?
			return identityRole.getRoleTreeNode() != null;
		}).forEach(identityRole -> {
			createConcept(roleRequest, identityRole, contract, identityRole.getRole(), identityRole.getRoleTreeNode(), ConceptRoleRequestOperation.UPDATE);
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
