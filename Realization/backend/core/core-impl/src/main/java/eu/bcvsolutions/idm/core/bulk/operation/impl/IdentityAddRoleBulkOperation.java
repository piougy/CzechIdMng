package eu.bcvsolutions.idm.core.bulk.operation.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.operation.AbstractBulkOperation;
import eu.bcvsolutions.idm.core.api.bulk.operation.dto.IdmBulkOperationDto;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.rest.impl.IdmIdentityController;

/**
 * Bulk operation for add role to identity
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component("identityAddRoleBulkOperation")
@Description("Add role to idetity in bulk operation.")
public class IdentityAddRoleBulkOperation extends AbstractBulkOperation<IdmIdentityDto> {

	public static final String BULK_OPERATION_NAME = "identity-add-role-bulk-operation";
	
	private static final String ROLE_CODE = "role";
	private static final String VALID_TILL_CODE = "validTill";
	private static final String VALID_FROM_CODE = "validFrom";
	private static final String APPROVE_CODE = "approve";
	
	@Autowired
	private IdmIdentityService identitySevice;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmRoleService roleService;
	
	@Override
	public boolean supports(Class<? extends AbstractEntity> clazz) {
		return clazz.isAssignableFrom(IdmIdentity.class);
	}

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		return super.getFormAttributes();
	}

	@Override
	public String getName() {
		return IdentityAddRoleBulkOperation.BULK_OPERATION_NAME;
	}

	@Override
	public Boolean process() {
		IdmBulkOperationDto operation = this.getOperation();
		Assert.notNull(operation);
		
		if (operation.getIdentifiers() != null) {
			return processBySet();
		} else {
			// transform filter
			resolveFilter(operation);
			return processByFilter();
		}
	}

	
	@Override
	public String getEntityClass() {
		return IdmIdentity.class.getName();
	}
	
	@Override
	public String getFilterClass() {
		return IdmIdentityFilter.class.getName();
	}
	
	private void resolveFilter(IdmBulkOperationDto operation) {
		BaseFilter filter = toFilter(operation.getProperties());
		this.setOperation(operation);
	}
	
	private Page<IdmIdentityDto> find(Pageable pageable) {
		IdmBulkOperationDto operation = this.getOperation();
		Assert.notNull(operation);
		BaseFilter filter = operation.getFilter();
		//
		if (filter instanceof IdmIdentityFilter) {
			IdmIdentityFilter identityFilter = (IdmIdentityFilter) filter;
			return identitySevice.find(identityFilter, pageable);
		}
		// TODO exception
		return null;
	}
	
	private Boolean processByFilter() {
		Page<IdmIdentityDto> toProcess = find(getPageable());
		//
		this.count = toProcess.getTotalElements();
		this.counter = 0l;
		//
		while(toProcess.hasContent()) {
			Boolean result = processIdentities(toProcess);
			if (result != null) {
				return result;
			}
			toProcess = find(toProcess.nextPageable());
		}
		return Boolean.TRUE;
	}
	
	private Boolean processBySet() {
		Set<String> identifiers = this.getOperation().getIdentifiers();
		//
		this.count = Long.valueOf(identifiers.size());
		this.counter = 0l;
		//
		// TODO: pageable, another than username
		for (String identifier : identifiers) {
			IdmIdentityDto identity = identitySevice.getByUsername(identifier);
			if (identity != null) {
				processIdentity(identity);
				this.counter++;
				if (!updateState()) {
					return Boolean.FALSE;
				} else {
					this.count--;
				}
			}
		}
		return Boolean.TRUE;
	}
	
	private Boolean processIdentities(Iterable<IdmIdentityDto> identities) {
		for (IdmIdentityDto identity : identities) {
			// check if can process identity
			if (canBeProcess(identity)) {
				// process identity
				processIdentity(identity);
				this.counter++;
				if (!updateState()) {
					return Boolean.FALSE;
				}
			} else {
				this.count--;
			}
		}
		return null;
	}
	
	private boolean canBeProcess(IdmIdentityDto identity) {
		Set<String> removeIdentifiers = this.getOperation().getRemoveIdentifiers();
		if (removeIdentifiers == null) {
			return true;
		}
		// TODO: now only username
		if (removeIdentifiers.contains(identity.getUsername())) {
			return false;
		}
		return true;
	}
	
	private void processIdentity(IdmIdentityDto identity) {
		// now get prime identity contract and only valid
		IdmIdentityContractDto contract = identityContractService.getPrimeValidContract(identity.getId());
		//
		if (contract == null) {
			// TODO
		}
		//
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
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
		}
		//
		roleRequestService.startRequest(roleRequest.getId(), getApprove());
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
		// TODO check if exists
		((Collection<?>) rolesAsObject).forEach(role -> {
			UUID uuid = EntityUtils.toUuid(role);
			roles.add(roleService.get(uuid));
		});
		return roles;
	}
	
	private boolean getApprove() {
		Boolean approve = this.getParameterConverter().toBoolean(getProperties(), APPROVE_CODE);
		return approve != null ? approve.booleanValue() : true;
	}
	
	private LocalDate getValidFrom() {
		return getParameterConverter().toLocalDate(getProperties(), VALID_FROM_CODE);
	}
	
	private LocalDate getValidTill() {
		return getParameterConverter().toLocalDate(getProperties(), VALID_TILL_CODE);
	}
	
	protected IdmFormAttributeDto getRoleAttribute() {
		IdmFormAttributeDto disabled = new IdmFormAttributeDto(
				ROLE_CODE, 
				"Roles", 
				PersistentType.UUID);
		disabled.setFaceType(BaseFaceType.ROLE_SELECT);
		disabled.setPlaceholder("Role select ...");
		return disabled;
	}
}
