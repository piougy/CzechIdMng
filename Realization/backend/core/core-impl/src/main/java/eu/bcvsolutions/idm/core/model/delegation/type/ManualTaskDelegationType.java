package eu.bcvsolutions.idm.core.model.delegation.type;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmDelegationFilter;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractDelegationType;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import org.springframework.stereotype.Component;

/**
 *
 * Delegation type for manual reassing a task in bulk action.
 * This type cannot be selected manually by user.
 *
 * @author Vít Švanda
 */
@Component(ManualTaskDelegationType.NAME)
public class ManualTaskDelegationType extends AbstractDelegationType {

	public static final String NAME = "manual-task-delegation-type";

	@Override
	public Class<? extends BaseDto> getOwnerType() {
		return WorkflowTaskInstanceDto.class;
	}
	
	@Override
	public IdmDelegationDto delegate(BaseDto owner, IdmDelegationDefinitionDto definition) {
		IdmDelegationFilter delegationFilter = new IdmDelegationFilter();
		delegationFilter.setOwnerId(DtoUtils.toUuid(owner.getId()));
		delegationFilter.setOwnerType(owner.getClass().getCanonicalName());
		delegationFilter.setDelegationDefinitionId(definition.getId());

		// Check if same delegation already exists is for manual delegatio disabled.

		IdmDelegationDto delegation = new IdmDelegationDto();
		delegation.setOwnerState(new OperationResultDto(OperationState.RUNNING));
		delegation.setDefinition(definition.getId());
		delegation.setOwnerId(DtoUtils.toUuid(owner.getId()));
		delegation.setOwnerType(owner.getClass().getCanonicalName());

		return delegationService.save(delegation);
	}

	@Override
	public boolean isSupportsDelegatorContract() {
		return false;
	}
	
	@Override
	public boolean canBeCreatedManually() {
		return false;
	}

	@Override
	public boolean sendNotifications() {
		return false;
	}

	@Override
	public boolean sendDelegationNotifications() {
		return true;
	}

	@Override
	public int getOrder() {
		return 30;
	}
}
