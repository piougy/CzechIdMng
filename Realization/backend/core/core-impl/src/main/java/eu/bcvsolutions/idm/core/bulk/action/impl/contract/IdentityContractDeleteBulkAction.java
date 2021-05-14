package eu.bcvsolutions.idm.core.bulk.action.impl.contract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.ExceptionUtils;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;

/**
 * Delete contracted positions.
 * 
 * @author Radek Tomiška
 * @since 9.7.3
 */
@Component(IdentityContractDeleteBulkAction.NAME)
@Description("Delete contracted positions.")
public class IdentityContractDeleteBulkAction extends AbstractRemoveBulkAction<IdmIdentityContractDto, IdmIdentityContractFilter> {

	public static final String NAME = "core-identity-contract-delete-bulk-action";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityContractDeleteBulkAction.class);
	//
	@Autowired private IdmIdentityContractService contractService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private EntityStateManager entityStateManager;
	//
	private final List<UUID> processedIds = new ArrayList<UUID>();
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.IDENTITYCONTRACT_DELETE);
	}
	
	@Override
	protected OperationResult processDto(IdmIdentityContractDto contract) {
		try {
			Map<String, Serializable> properties = new HashMap<>();
			properties.put(EntityEventProcessor.PROPERTY_FORCE_DELETE, Boolean.TRUE); // force delete by default - ensures asynchronous processing
			IdentityContractEvent identityContractEvent = new IdentityContractEvent(IdentityContractEventType.DELETE, contract, properties);
			identityContractEvent.setPriority(PriorityType.HIGH);
			EventContext<IdmIdentityContractDto> result = contractService.publish(identityContractEvent);
			processedIds.add(result.getContent().getId());
			//
			return new OperationResult.Builder(OperationState.EXECUTED).build();
		} catch (ResultCodeException ex) {
			return new OperationResult.Builder(OperationState.EXCEPTION).setException(ex).build();
		} catch (Exception ex) {
			Throwable resolvedException = ExceptionUtils.resolveException(ex);
			if (resolvedException instanceof ResultCodeException) {
				return new OperationResult.Builder(OperationState.EXCEPTION) //
						.setException((ResultCodeException) resolvedException) //
						.build(); //
			}
			return new OperationResult.Builder(OperationState.EXCEPTION).setCause(ex).build();
		}
	}
	
	@Override
	protected OperationResult end(OperationResult result, Exception exception) {
		if (exception != null 
				|| (result != null && OperationState.EXECUTED != result.getState())) {
			return super.end(result, exception);
		}
		// success - force by default
		for (UUID contractId : processedIds) {
			IdmIdentityContractDto contract = getService().get(contractId);
			if (contract != null) {
				// check assigned roles again - can be assigned in the meantime ...
				IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
				identityRoleFilter.setIdentityContractId(contractId);
				if (identityRoleService.count(identityRoleFilter) > 0) {		
					return super.end(
							result, 
							new ResultCodeException(
									CoreResultCode.CONTRACT_DELETE_FAILED_ROLE_ASSIGNED,
									ImmutableMap.of("contract", contractId)
							)
					);
				}
				contractService.deleteInternal(contract);
				//
				LOG.debug("Contract [{}] deleted.", contractId);
			} else {
				LOG.debug("Contract [{}] already deleted.", contractId);
			}
			// clean up all states
			entityStateManager.deleteStates(new IdmIdentityContractDto(contractId), null, null);
		}
		return super.end(result, exception);
	}

	@Override
	public ReadWriteDtoService<IdmIdentityContractDto, IdmIdentityContractFilter> getService() {
		return contractService;
	}
}
