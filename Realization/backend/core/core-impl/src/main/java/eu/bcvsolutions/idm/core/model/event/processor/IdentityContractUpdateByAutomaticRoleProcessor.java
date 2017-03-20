package eu.bcvsolutions.idm.core.model.event.processor;

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
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
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
	private IdentityContractCreateByAutomaticRoleProcessor createProcessor;
	
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
		IdmIdentityContract previous = repository.getPersistedIdentityContract(contract);
		IdmTreeNode newPosition = contract.getWorkingPosition();
		//
		// check automatic roles - if position or disabled was changed
		if (!Objects.equals(newPosition, previous.getWorkingPosition())
				|| (!contract.isDisabled() && previous.isDisabled() != contract.isDisabled())) {
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
							identityRoleService.delete(identityRole);
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
			// change date - for unchanged assigned roles only
			if (EntityUtils.validableChanged(previous, contract)) {
				changeValidable(contract, assignedRoles);
			}
			// assign new roles
			createProcessor.assignAutomaticRoles(contract, addedAutomaticRoles);
		}
		//
		// process validable change
		else if (EntityUtils.validableChanged(previous, contract)) {
			changeValidable(contract, identityRoleService.getRoles(contract));
		}
		//
		return new DefaultEventResult<>(event, this);
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
	 * TODO: connect to role request API 
	 * 
	 * @param contract
	 * @param assignedRoles
	 */
	private void changeValidable(IdmIdentityContract contract, List<IdmIdentityRole> assignedRoles) {
		assignedRoles.stream()
		.filter(identityRole -> {
			// TODO: automatic roles only?
			return identityRole.getRoleTreeNode() != null;
		}).forEach(identityRole -> {
			identityRole.setValidFrom(contract.getValidFrom());
			identityRole.setValidTill(contract.getValidTill());
			// TODO: connect to role request API 
			identityRoleService.save(identityRole);
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
