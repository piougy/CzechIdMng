package eu.bcvsolutions.idm.core.model.event.processor.contract;

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

import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ContractPositionProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmContractPosition_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.event.ContractPositionEvent.ContractPositionEventType;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;

/**
 * Automatic roles recount while contract position is created or updated.
 * 
 * @author Radek Tomiška
 * @since 9.1.0
 */
@Component(ContractPositionAutomaticRoleProcessor.PROCESSOR_NAME)
@Description("Automatic roles recount while contract position is created or updated.")
public class ContractPositionAutomaticRoleProcessor
		extends CoreEventProcessor<IdmContractPositionDto> 
		implements ContractPositionProcessor {
	
	public static final String PROCESSOR_NAME = "core-contract-position-automatic-role-processor";
	//
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	
	public ContractPositionAutomaticRoleProcessor() {
		super(ContractPositionEventType.NOTIFY);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmContractPositionDto> process(EntityEvent<IdmContractPositionDto> event) {
		IdmContractPositionDto contractPosition = event.getContent();
		IdmIdentityContractDto contract = DtoUtils.getEmbedded(contractPosition, IdmContractPosition_.identityContract);
		//
		UUID newPosition = contractPosition.getWorkPosition();
		//
		// check if new and old work position are same
		// check automatic roles - if position or disabled was changed
		if (contract.isValidNowOrInFuture()) {
			// work positions has some difference or validity changes
			List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByContractPosition(contractPosition.getId());
			//
			// remove all automatic roles by attribute
			// and automatic roles given by contracts position
			if (!assignedRoles.isEmpty()) {
				assignedRoles = assignedRoles
						.stream()
						.filter(autoRole -> {
							// just for sure
							AbstractIdmAutomaticRoleDto automaticRoleDto = DtoUtils.getEmbedded(autoRole, IdmIdentityRole_.automaticRole, (AbstractIdmAutomaticRoleDto) null);
							if (automaticRoleDto instanceof IdmRoleTreeNodeDto) {
								return true;
							}
							return false;
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
							roleTreeNodeService.removeAutomaticRoles(identityRole, null);
							iter.remove();
						} else {
							// change relation only
							identityRole.setAutomaticRole(addedAutomaticRole.getId());
							updateIdentityRole(identityRole);
							//
							// new automatic role is not needed
							addedAutomaticRoles.remove(addedAutomaticRole);
						}
					}
			    }
			}
			//
			// add identity roles
			roleTreeNodeService.addAutomaticRoles(contractPosition, addedAutomaticRoles);			
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
	 * Saves identity role by event - skip authorities check is needed (optimalizations)
	 * 
	 * @param identityRole
	 */
	private void updateIdentityRole(IdmIdentityRoleDto identityRole) {
		// skip check granted authorities
		IdentityRoleEvent event = new IdentityRoleEvent(IdentityRoleEventType.UPDATE, identityRole);
		event.getProperties().put(IdmIdentityRoleService.SKIP_CHECK_AUTHORITIES, Boolean.TRUE);
		identityRoleService.publish(event);
	}
	
	/**
	 * after save
	 */
	@Override
	public int getOrder() {
		return super.getOrder() + 500;
	}

}
