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
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ContractPositionProcessor;
import eu.bcvsolutions.idm.core.api.service.AutomaticRoleManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmContractPosition_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.event.ContractPositionEvent.ContractPositionEventType;

/**
 * Automatic roles by tree structure recount while contract position is created or updated.
 * 
 * @author Radek Tomiška
 * @since 9.1.0
 */
@Component(ContractPositionAutomaticRoleProcessor.PROCESSOR_NAME)
@Description("Automatic roles by tree structure recount while contract position is created or updated.")
public class ContractPositionAutomaticRoleProcessor
		extends CoreEventProcessor<IdmContractPositionDto> 
		implements ContractPositionProcessor {
	
	public static final String PROCESSOR_NAME = "core-contract-position-automatic-role-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContractPositionAutomaticRoleProcessor.class);
	//
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private EntityStateManager entityStateManager;
	
	public ContractPositionAutomaticRoleProcessor() {
		super(ContractPositionEventType.NOTIFY);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmContractPositionDto> event) {
		IdmIdentityContractDto contract = DtoUtils.getEmbedded(event.getContent(), IdmContractPosition_.identityContract);
		//
		return super.conditional(event)
				&& contract.isValidNowOrInFuture(); // invalid contracts cannot have roles (roles for disabled contracts are removed by different process)
	}

	@Override
	public EventResult<IdmContractPositionDto> process(EntityEvent<IdmContractPositionDto> event) {
		// when automatic role recalculation is skipped, then flag for contract position is created only
		// flag can be processed afterwards
		if (getBooleanProperty(AutomaticRoleManager.SKIP_RECALCULATION, event.getProperties())) {
			IdmContractPositionDto contractPosition = event.getContent();
			LOG.debug("Automatic roles are skipped for position [{}], state [AUTOMATIC_ROLE_SKIPPED] for position will be created only.",
					contractPosition.getId());
			// 
			entityStateManager.createState(contractPosition, OperationState.BLOCKED, CoreResultCode.AUTOMATIC_ROLE_SKIPPED, null);
			//
			return new DefaultEventResult<>(event, this);
		}
		//
		IdmContractPositionDto contractPosition = event.getContent();
		IdmIdentityContractDto contract = DtoUtils.getEmbedded(contractPosition, IdmContractPosition_.identityContract);
		//
		UUID newPosition = contractPosition.getWorkPosition();
		//
		// check automatic roles - if position or contract was enabled
		// work positions has some difference or validity changes
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByContractPosition(contractPosition.getId());
		//
		// remove all automatic roles by attribute
		// and automatic roles given by contracts position
		if (!assignedRoles.isEmpty()) {
			assignedRoles = assignedRoles
					.stream()
					.filter(autoRole -> {
						// just for sure, other contract position supports automatic role by tree structure only for now
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
		if (newPosition != null) {
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
		List<IdmConceptRoleRequestDto> concepts = new ArrayList<>(removedAutomaticRoles.size() + addedAutomaticRoles.size());
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
						concepts.add(conceptRoleRequest);
						//
						iter.remove();
					} else {
						// change relation only
						IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
						conceptRoleRequest.setIdentityRole(identityRole.getId());
						conceptRoleRequest.setAutomaticRole(addedAutomaticRole.getId());
						conceptRoleRequest.setIdentityContract(contract.getId());
						conceptRoleRequest.setContractPosition(contractPosition.getId());
						conceptRoleRequest.setValidFrom(contract.getValidFrom());
						conceptRoleRequest.setValidTill(contract.getValidTill());
						conceptRoleRequest.setRole(identityRole.getRole());
						conceptRoleRequest.setOperation(ConceptRoleRequestOperation.UPDATE);
						//
						concepts.add(conceptRoleRequest);
						//
						// new automatic role is not needed
						addedAutomaticRoles.remove(addedAutomaticRole);
					}
				}
		    }
		}
		//
		// add identity roles
		for (AbstractIdmAutomaticRoleDto autoRole : addedAutomaticRoles) {
			IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
			conceptRoleRequest.setIdentityContract(contract.getId());
			conceptRoleRequest.setContractPosition(contractPosition.getId());
			conceptRoleRequest.setValidFrom(contract.getValidFrom());
			conceptRoleRequest.setValidTill(contract.getValidTill());
			conceptRoleRequest.setRole(autoRole.getRole());
			conceptRoleRequest.setAutomaticRole(autoRole.getId());
			conceptRoleRequest.setOperation(ConceptRoleRequestOperation.ADD);
			//
			concepts.add(conceptRoleRequest);
		}
		//
		if (!concepts.isEmpty()) {
			IdmRoleRequestDto roleRequest = (IdmRoleRequestDto) event.getProperties().get(IdentityContractUpdateByAutomaticRoleProcessor.EVENT_PROPERTY_REQUEST);
			if (roleRequest != null) {
				// add concept into single request
				// single request will be executed by parent event
				roleRequest.getConceptRoles().addAll(concepts);
			} else {
				// execute new request
				roleRequest = roleRequestService.executeConceptsImmediate(contract.getIdentity(), concepts);
			}
			event.getProperties().put(IdentityContractUpdateByAutomaticRoleProcessor.EVENT_PROPERTY_REQUEST, roleRequest);
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
	 * after save
	 */
	@Override
	public int getOrder() {
		return super.getOrder() + 500;
	}

}
