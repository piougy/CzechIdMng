package eu.bcvsolutions.idm.acc.event.processor.provisioning;

import java.text.MessageFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Archives processed provisioning operations.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Archives processed provisioning operation.")
public class RemoveProcessedOperationProcessor extends AbstractEntityEventProcessor<SysProvisioningOperationDto> {
	
	public static final String PROCESSOR_NAME = "remove-processed-operation-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RemoveProcessedOperationProcessor.class);
	private final SysProvisioningOperationService provisioningOperationService;
	private final SysSystemEntityService systemEntityService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	
	@Autowired
	public RemoveProcessedOperationProcessor(
			SysProvisioningOperationService provisioningOperationService,
			SysProvisioningArchiveService provisioningArchiveService,
			SysSystemEntityService systemEntityService) {
		super(ProvisioningEventType.CREATE, ProvisioningEventType.UPDATE, ProvisioningEventType.DELETE, ProvisioningEventType.CANCEL);
		//
		Assert.notNull(provisioningOperationService);
		Assert.notNull(systemEntityService);		
		//
		this.provisioningOperationService = provisioningOperationService;
		this.systemEntityService = systemEntityService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<SysProvisioningOperationDto> process(EntityEvent<SysProvisioningOperationDto> event) {
		SysProvisioningOperationDto provisioningOperation = event.getContent();
		if (OperationState.EXECUTED == provisioningOperation.getResultState() 
				|| ProvisioningEventType.CANCEL == event.getType()) {
			provisioningOperationService.deleteOperation(provisioningOperation);
			LOG.debug("Executed provisioning operation [{}] was removed from queue.", provisioningOperation.getId());
			//
			if (ProvisioningEventType.DELETE == event.getType()) {
				// We successfully deleted account on target system. We need to delete system entity
				systemEntityService.deleteById(provisioningOperation.getSystemEntity());
			}
			
			UUID roleRequestId = provisioningOperation.getRoleRequestId();
			if (roleRequestId != null) {
				// Check of the state for whole request
				// Create mock request -> we don't wont load request from DB -> optimization
				IdmRoleRequestDto mockRequest = new IdmRoleRequestDto();
				mockRequest.setId(roleRequestId);
				mockRequest.setState(RoleRequestState.EXECUTED);

				IdmRoleRequestDto returnedReqeust = roleRequestService.refreshSystemState(mockRequest);
				OperationResultDto systemState = returnedReqeust.getSystemState(); 
				if (systemState == null) {
					// State on system of request was not changed (may be not all provisioning operations are
					// resolved)
				} else {
					// We have final state on systems
					IdmRoleRequestDto requestDto = roleRequestService.get(roleRequestId);
					if (requestDto != null) {
						requestDto.setSystemState(systemState);
						roleRequestService.save(requestDto);
					} else {
						LOG.info(MessageFormat.format(
								"Refresh role-request system state: Role-request with ID [{0}] was not found (maybe was deleted).",
								roleRequestId));
					}
				}
			}
		}
		return new DefaultEventResult<>(event, this, isClosable());
	}
	
	@Override
	public boolean isClosable() {
		return true;
	}
	
	@Override
	public int getOrder() {
		// on the end
		return 5000;
	}
}
