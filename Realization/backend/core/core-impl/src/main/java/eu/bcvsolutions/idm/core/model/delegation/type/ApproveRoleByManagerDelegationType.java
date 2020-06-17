package eu.bcvsolutions.idm.core.model.delegation.type;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractDelegationType;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.apache.commons.collections.CollectionUtils;
import org.modelmapper.internal.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * Delegation type for workflow tasks in process 'approve-role-by-manager'.
 *
 * Designed for the 'approve-role-by-manager' workflow process. Delegates by contract.
 *
 * @author Vít Švanda
 */
@Component(ApproveRoleByManagerDelegationType.NAME)
public class ApproveRoleByManagerDelegationType extends AbstractDelegationType {

	public static final String NAME = "approve-role-by-manager-delegation-type";
	private static final String ROLE_CONCEPT_PROPERTY = "conceptRole";
	
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private WorkflowTaskInstanceService taskInstanceService;

	@Override
	public List<IdmDelegationDefinitionDto> findDelegation(UUID delegatorId, UUID contractId, BaseDto owner) {
		Assert.isTrue(owner instanceof WorkflowTaskInstanceDto, "Owner must be workflow task for this delegation type!");
		WorkflowTaskInstanceDto task = (WorkflowTaskInstanceDto) owner;
		Object conceptRoleObj = taskInstanceService.getProcessVariable(task.getId(), ROLE_CONCEPT_PROPERTY);
		if (conceptRoleObj instanceof VariableInstance) {
			VariableInstance variableInstance = (VariableInstance) conceptRoleObj;
			conceptRoleObj = variableInstance.getValue();
		}
		if (!(conceptRoleObj instanceof IdmConceptRoleRequestDto)) {
			throw new CoreException(MessageFormat.format("For this delegation type [{0}], must workflow task contains '{1}' [WorkflowTaskInstanceDto] variable",
					NAME,
					ROLE_CONCEPT_PROPERTY));
		}
		IdmConceptRoleRequestDto conceptRole = (IdmConceptRoleRequestDto) conceptRoleObj;
		UUID contractFromRequest = conceptRole.getIdentityContract();
		Assert.notNull(contractFromRequest, "Contract ID from the concept of the request cannot be null!");
		
		// Filtering managers contracts by subordinates contract.
		IdmIdentityContractFilter contractFilter = new IdmIdentityContractFilter();
		contractFilter.setIdentity(delegatorId);
		contractFilter.setManagersByContract(contractFromRequest);
		
		List<IdmIdentityContractDto> managersContracts = identityContractService.find(contractFilter, null).getContent();
		
		if (managersContracts.isEmpty()) {
			return null;
		}
		Set<IdmDelegationDefinitionDto> resultDelegations = Sets.newHashSet();
		// Find all valid delegation definitions for manager's contract.
		managersContracts.stream()
				.map((managersContract) -> super.findDelegation(delegatorId, managersContract.getId(), owner))
				.filter((delegations) -> (!CollectionUtils.isEmpty(delegations)))
				.forEachOrdered((delegations) -> {
					resultDelegations.addAll(delegations);
				});
		return Lists.newArrayList(resultDelegations);
	}

	@Override
	public Class<? extends BaseDto> getOwnerType() {
		return WorkflowTaskInstanceDto.class;
	}

	@Override
	public boolean isSupportsDelegatorContract() {
		return true;
	}

	@Override
	public int getOrder() {
		return 30;
	}
}
