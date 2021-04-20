package eu.bcvsolutions.idm.core.bulk.action.impl.contract;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Bulk operation for removing contract guarantees
 *
 * @author Ondrej Husnik
 *
 */
@Enabled(CoreModuleDescriptor.MODULE_ID)
@Component(IdentityRemoveContractGuaranteeBulkAction.NAME)
@Description("Remove contract guarantee from idetity in bulk action.")
public class IdentityRemoveContractGuaranteeBulkAction extends AbstractContractGuaranteeBulkAction {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityRemoveContractGuaranteeBulkAction.class);

	public static final String NAME = "identity-remove-contract-guarantee-bulk-action";
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		formAttributes.add(getGuaranteeAttribute(PROPERTY_OLD_GUARANTEE, true, true));
		return formAttributes;
	}
	
	@Override
	public List<String> getAuthorities() {
		List<String> permissions = super.getAuthorities();
		permissions.add(CoreGroupPermission.CONTRACTGUARANTEE_DELETE);
		return permissions;
	}

	@Override
	public String getName() {
		return IdentityRemoveContractGuaranteeBulkAction.NAME;
	}
	
	@Override
	public int getOrder() {
		return DEFAULT_ORDER + 501;
	}

	@Override
	protected OperationResult processDto(IdmIdentityDto identity) {
		Set<UUID> selectedGuarantees = getSelectedGuaranteeUuids(PROPERTY_OLD_GUARANTEE);
		Map<UUID, List<IdmContractGuaranteeDto>> currentGuarantees = getIdentityGuaranteesOrderedByContract(identity.getId());
		currentGuarantees.forEach((contractId, contractGuarantees) -> {
			// create list of guarantee dtos to delete
			List<IdmContractGuaranteeDto> toDelete = contractGuarantees
					.stream()
					.filter(guarantee -> selectedGuarantees.contains(guarantee.getGuarantee()))
					.collect(Collectors.toList());
			// delete guarantees
			for (IdmContractGuaranteeDto guarantee : toDelete) {
				try {
					contractGuaranteeService.delete(guarantee, IdmBasePermission.DELETE);
					logItemProcessed(guarantee, new OperationResult.Builder(OperationState.EXECUTED).build());
				} catch (ForbiddenEntityException ex) {
					LOG.warn("Not authorized to remove contract guarantee [{}] of contract [{}].", guarantee.getGuarantee(), contractId, ex);
					logContractGuaranteePermissionError(guarantee, guarantee.getGuarantee(), contractId, IdmBasePermission.DELETE, ex);
				} catch (ResultCodeException ex) {
					logResultCodeException(guarantee, ex);
				}
			}
		});
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
}
