package eu.bcvsolutions.idm.core.bulk.action.impl.contract;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Bulk operation for adding contract guarantees
 *
 * @author Ondrej Husnik
 *
 */
@Enabled(CoreModuleDescriptor.MODULE_ID)
@Component(IdentityAddContractGuaranteeBulkAction.NAME)
@Description("Add contract guarantee to idetity in bulk action.")
public class IdentityAddContractGuaranteeBulkAction extends AbstractContractGuaranteeBulkAction {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityAddContractGuaranteeBulkAction.class);

	public static final String NAME = "identity-add-contract-guarantee-bulk-action";
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		formAttributes.add(getGuaranteeAttribute(PROPERTY_NEW_GUARANTEE, false, true));
		return formAttributes;
	}
	
	@Override
	public List<String> getAuthorities() {
		List<String> permissions = super.getAuthorities();
		permissions.add(CoreGroupPermission.CONTRACTGUARANTEE_CREATE);
		return permissions;
	}

	@Override
	public String getName() {
		return IdentityAddContractGuaranteeBulkAction.NAME;
	}
	
	@Override
	public int getOrder() {
		return DEFAULT_ORDER + 500;
	}

	@Override
	protected OperationResult processDto(IdmIdentityDto identity) {
		Set<UUID> newGuarantees = getSelectedGuaranteeUuids(PROPERTY_NEW_GUARANTEE);
		
		Map<UUID, List<IdmContractGuaranteeDto>> currentGuarantees = getIdentityGuaranteesOrderedByContract(identity.getId());
		// iterate over all contract UUIDs ~ keys and contractGuarantees in List ~ values
		
		for (Map.Entry<UUID, List<IdmContractGuaranteeDto>> entry : currentGuarantees.entrySet()) {
			UUID contractId = entry.getKey();
			List<IdmContractGuaranteeDto> contractGuarantees = entry.getValue();
		
			Set<UUID> currentGuaranteesUuidSet = contractGuarantees.stream().map(IdmContractGuaranteeDto::getGuarantee).collect(Collectors.toSet());
			Set<UUID> guaranteesToAdd = Sets.difference(newGuarantees, currentGuaranteesUuidSet);
			// add all new contract guarantees
			for (UUID guaranteeId : guaranteesToAdd) {
				try {
					IdmContractGuaranteeDto guaranteeDto = createContractGuarantee(guaranteeId, contractId, IdmBasePermission.CREATE);
					logItemProcessed(guaranteeDto, new OperationResult.Builder(OperationState.EXECUTED).build());
				} catch (ForbiddenEntityException ex) {
					LOG.warn("Not authorized to set contract guarantee [{}] of contract [{}].", guaranteeId, contractId, ex);
					IdmIdentityContractDto dto = identityContractService.get(contractId);
					logContractGuaranteePermissionError(dto, guaranteeId, contractId, IdmBasePermission.CREATE, ex);
				} catch (ResultCodeException ex) {
					IdmIdentityContractDto dto = identityContractService.get(contractId);
					logResultCodeException(dto, ex);
				}
			}
		}
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	
}
