package eu.bcvsolutions.idm.core.bulk.action.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * Bulk operation for add role to identity
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component("identityAddRoleBulkAction")
@Description("Add role to idetity in bulk action.")
public class IdentityAddRoleBulkAction extends AbstractIdentityBulkAction {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityAddRoleBulkAction.class);
	
	public static final String BULK_ACTION_NAME = "identity-add-role-bulk-action";
	
	private static final String ROLE_CODE = "role";
	private static final String PRIMARY_CONTRACT_CODE = "mainContract";
	private static final String VALID_TILL_CODE = "validTill";
	private static final String VALID_FROM_CODE = "validFrom";
	private static final String APPROVE_CODE = "approve";
	
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		formAttributes.add(getRoleAttribute());
		formAttributes.add(getApproveAttribute());
		formAttributes.add(getPrimaryContractAttribute());
		return formAttributes;
	}

	@Override
	public String getName() {
		return IdentityAddRoleBulkAction.BULK_ACTION_NAME;
	}

	protected void processIdentity(IdmIdentityDto identity) {
		List<IdmIdentityContractDto> contracts = new ArrayList<>();
		if (this.isPrimaryContract()) {
			IdmIdentityContractDto contract = identityContractService.getPrimeValidContract(identity.getId());
			//
			if (contract == null) {
				LOG.warn("For identity id: [{}] username: [{}] wasn't found contranct.", identity.getId(), identity.getUsername());
				return;
			}
			contracts.add(contract);
		} else {
			contracts.addAll(identityContractService.findAllByIdentity(identity.getId()));
		}
		//
		boolean approve = isApprove();
		for (IdmIdentityContractDto contract : contracts) {
			IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
			roleRequest.setApplicant(contract.getIdentity());
			roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
			roleRequest.setLog("Request was created by bulk action.");
			roleRequest = roleRequestService.save(roleRequest);
			//
			LocalDate validFrom = this.getValidFrom();
			LocalDate validTill = this.getValidTill();
			for (IdmRoleDto role : getRoles()) {
				IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
				concept.setRole(role.getId());
				concept.setIdentityContract(contract.getId());
				concept.setOperation(ConceptRoleRequestOperation.ADD);
				concept.setValidFrom(validFrom);
				concept.setValidTill(validTill);
				concept.setRoleRequest(roleRequest.getId());
				concept = conceptRoleRequestService.save(concept);
			}
			//
			roleRequestService.startRequest(roleRequest.getId(), approve);
		}
	}
	
	private List<IdmRoleDto> getRoles() {
		Object rolesAsObject = this.getProperties().get(ROLE_CODE);
		//
		if (rolesAsObject == null) {
			return Collections.emptyList();
		}
		//
		if (!(rolesAsObject instanceof Collection)) {
			return Collections.emptyList();
		}
		List<IdmRoleDto> roles = new ArrayList<>();
		((Collection<?>) rolesAsObject).forEach(role -> {
			UUID uuid = EntityUtils.toUuid(role);
			IdmRoleDto roleDto = roleService.get(uuid);
			if (roleDto == null) {
				LOG.warn("Role with id [{}] not found. The role will be skipped.", uuid);
			} else {
				roles.add(roleService.get(uuid));
			}
		});
		return roles;
	}
	
	private boolean isApprove() {
		Boolean approve = this.getParameterConverter().toBoolean(getProperties(), APPROVE_CODE);
		return approve != null ? approve.booleanValue() : true;
	}
	
	private boolean isPrimaryContract() {
		Boolean approve = this.getParameterConverter().toBoolean(getProperties(), PRIMARY_CONTRACT_CODE);
		return approve != null ? approve.booleanValue() : true;
	}
	
	private LocalDate getValidFrom() {
		return getParameterConverter().toLocalDate(getProperties(), VALID_FROM_CODE);
	}
	
	private LocalDate getValidTill() {
		return getParameterConverter().toLocalDate(getProperties(), VALID_TILL_CODE);
	}
	
	protected IdmFormAttributeDto getApproveAttribute() {
		IdmFormAttributeDto approve = new IdmFormAttributeDto(
				APPROVE_CODE, 
				APPROVE_CODE, 
				PersistentType.BOOLEAN);
		approve.setDefaultValue(Boolean.TRUE.toString());
		return approve;
	}
	
	protected IdmFormAttributeDto getRoleAttribute() {
		IdmFormAttributeDto roles = new IdmFormAttributeDto(
				ROLE_CODE, 
				ROLE_CODE, 
				PersistentType.UUID);
		roles.setFaceType(BaseFaceType.ROLE_SELECT);
		roles.setMultiple(true);
		roles.setRequired(true);
		return roles;
	}

	protected IdmFormAttributeDto getPrimaryContractAttribute() {
		IdmFormAttributeDto primaryContract = new IdmFormAttributeDto(
				PRIMARY_CONTRACT_CODE, 
				PRIMARY_CONTRACT_CODE, 
				PersistentType.BOOLEAN);
		primaryContract.setDefaultValue(Boolean.TRUE.toString());
		return primaryContract;
	}
}
