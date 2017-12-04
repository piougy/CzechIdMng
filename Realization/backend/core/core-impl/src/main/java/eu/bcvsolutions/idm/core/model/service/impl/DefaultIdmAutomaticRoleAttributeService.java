package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmAutomaticRoleAttribute;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleAttributeRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Automatic role by attribute
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service("automaticRoleAttributeService")
public class DefaultIdmAutomaticRoleAttributeService
	extends AbstractReadWriteDtoService<IdmAutomaticRoleAttributeDto, IdmAutomaticRoleAttribute, IdmAutomaticRoleFilter>
	implements IdmAutomaticRoleAttributeService {

	private final IdmRoleRequestService roleRequestService;
	private final IdmIdentityContractService identityContractService;
	private final IdmConceptRoleRequestService conceptRoleRequestService;
	
	@Autowired
	public DefaultIdmAutomaticRoleAttributeService(IdmAutomaticRoleAttributeRepository repository,
			IdmRoleRequestService roleRequestService,
			IdmIdentityContractService identityContractService,
			IdmConceptRoleRequestService conceptRoleRequestService,
			EntityEventManager entityEventManager) {
		super(repository);
		//
		Assert.notNull(roleRequestService);
		Assert.notNull(identityContractService);
		Assert.notNull(conceptRoleRequestService);
		//
		this.roleRequestService = roleRequestService;
		this.identityContractService = identityContractService;
		this.conceptRoleRequestService = conceptRoleRequestService;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.AUTOMATICROLEATTRIBUTE, getEntityClass());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public IdmRoleRequestDto prepareRemoveAutomaticRoles(IdmIdentityRoleDto identityRole,
			Set<AbstractIdmAutomaticRoleDto> automaticRoles) {
		Assert.notNull(identityRole);
		//
		IdmIdentityContractDto dto = identityContractService.get(identityRole.getIdentityContract());
		return this.processAutomaticRoles(dto, identityRole.getId(), automaticRoles, ConceptRoleRequestOperation.REMOVE);
	}
	
	private IdmRoleRequestDto processAutomaticRoles(IdmIdentityContractDto contract, UUID identityRoleId,
			Set<AbstractIdmAutomaticRoleDto> automaticRoles, ConceptRoleRequestOperation operation) {
		Assert.notNull(automaticRoles);
		Assert.notNull(contract);
		Assert.notNull(operation);
		//
		if (automaticRoles.isEmpty()) {
			return null;
		}
		//
		// prepare request
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		roleRequest.setApplicant(contract.getIdentity());
		roleRequest.setRequestedByType(RoleRequestedByType.AUTOMATICALLY);
		roleRequest.setExecuteImmediately(true); // TODO: by configuration
		roleRequest = roleRequestService.save(roleRequest);
		//
		for(AbstractIdmAutomaticRoleDto roleTreeNode : automaticRoles) {
			IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
			conceptRoleRequest.setRoleRequest(roleRequest.getId());
			conceptRoleRequest.setIdentityContract(contract.getId());
			conceptRoleRequest.setValidFrom(contract.getValidFrom());
			conceptRoleRequest.setIdentityRole(identityRoleId);
			conceptRoleRequest.setValidTill(contract.getValidTill());
			conceptRoleRequest.setRole(roleTreeNode.getRole());
			conceptRoleRequest.setRoleTreeNode(roleTreeNode.getId());
			//
			conceptRoleRequest.setOperation(operation);
			//
			conceptRoleRequestService.save(conceptRoleRequest);
		};
		//
		return roleRequest;
	}
}