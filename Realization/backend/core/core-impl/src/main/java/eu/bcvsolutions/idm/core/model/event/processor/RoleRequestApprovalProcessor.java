package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Strings;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;

/**
 * Approve requested permission changes processor
 * 
 * @author svandav
 *
 */
@Component
@Description("Approve requested permission changes. By default will be started workflow with definition '"+RoleRequestApprovalProcessor.DEFAULT_WF_PROCESS_NAME+"'.")
public class RoleRequestApprovalProcessor extends CoreEventProcessor<IdmRoleRequestDto> {
	
	public static final String PROCESSOR_NAME = "role-request-approval-processor";
	public static final String PROPERTY_WF = "wf";
	public static final String CHECK_RIGHT_PROPERTY = "checkRight";
	public static final String DEFAULT_WF_PROCESS_NAME = "approve-identity-change-permissions";
	
	private final IdmRoleRequestService service;
	
	@Autowired
	public RoleRequestApprovalProcessor(IdmRoleRequestService service) {
		super(RoleRequestEventType.EXCECUTE); 
		//
		Assert.notNull(service);
		//
		this.service = service;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleRequestDto> process(EntityEvent<IdmRoleRequestDto> event) {
		IdmRoleRequestDto dto = event.getContent();
		boolean checkRight = (boolean) event.getProperties().get(CHECK_RIGHT_PROPERTY);
		//
		String wfDefinition = getConfigurationProperty(PROPERTY_WF);
		if(Strings.isNullOrEmpty(wfDefinition)){
			wfDefinition = DEFAULT_WF_PROCESS_NAME;
		}
		boolean approved = service.startApprovalProcess(dto, checkRight, event, wfDefinition);
		DefaultEventResult<IdmRoleRequestDto> result = new DefaultEventResult<>(event, this);
		result.setSuspended(!approved);
		return result;
	}
	
	/**
	 * Before standard save
	 * 
	 * @return
	 */
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER - 1000;
	}
}
