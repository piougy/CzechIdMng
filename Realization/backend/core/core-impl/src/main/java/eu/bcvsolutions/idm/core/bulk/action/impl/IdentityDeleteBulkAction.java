package eu.bcvsolutions.idm.core.bulk.action.impl;

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

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.ExceptionUtils;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Delete given identities.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@SuppressWarnings("deprecation")
@Component(IdentityDeleteBulkAction.NAME)
@Description("Delete given identities.")
@Enabled(module = CoreModuleDescriptor.MODULE_ID, property = IdentityConfiguration.PROPERTY_IDENTITY_DELETE)
public class IdentityDeleteBulkAction extends AbstractRemoveBulkAction<IdmIdentityDto, IdmIdentityFilter> {

	public static final String NAME = "identity-delete-bulk-action";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityDeleteBulkAction.class);
	//
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmIdentityContractService contractService;
	@Autowired
	private EntityStateManager entityStateManager;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	//
	private final List<UUID> processedIds = new ArrayList<UUID>();

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.IDENTITY_DELETE);
	}
	
	@Override
	protected OperationResult processDto(IdmIdentityDto identity) {
		try {
			Map<String, Serializable> properties = new HashMap<>();
			properties.put(EntityEventProcessor.PROPERTY_FORCE_DELETE, Boolean.TRUE); // force delete by default - ensures asynchronous processing
			EventContext<IdmIdentityDto> result = identityService.publish(
					new IdentityEvent(IdentityEventType.DELETE, identity, properties)
			);
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
		for (UUID identityId : processedIds) {
			IdmIdentityDto identity = getService().get(identityId);
			if (identity != null) {
				// delete identity contracts => contract related records are  removed asynchornously, but contract itself will be removed here
				for (IdmIdentityContractDto contract : contractService.findAllByIdentity(identityId)) {
					// check assigned roles again - can be assigned in the meantime ...
					IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
					UUID contractId = contract.getId();
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
					// clean up all states
					entityStateManager.deleteStates(contract, null, null);
				}
				//
				// Delete all role requests where is this identity applicant - processed asynchronous requests should be deleted here
				IdmRoleRequestFilter roleRequestFilter = new IdmRoleRequestFilter();
				roleRequestFilter.setApplicantId(identityId);
				roleRequestService
					.find(roleRequestFilter, null)
					.forEach(request ->{
						roleRequestService.delete(request);
					});
				//
				identityService.deleteInternal(identity);
				//
				LOG.debug("Identity [{}] deleted.", identity.getUsername());
			} else {
				LOG.debug("Identity [{}] already deleted.", identityId);
			}
			// clean up all states
			entityStateManager.deleteStates(new IdmIdentityDto(identityId), null, null);
		}
		return super.end(result, exception);
	}

	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}
}
