package eu.bcvsolutions.idm.core.model.event.processor.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.RequestManager;
import eu.bcvsolutions.idm.core.model.event.RequestEvent.RequestEventType;

/**
 * Approve changes in the request.
 * 
 * @author svandav
 *
 */
@Component
@Description("Approve changes in the request.")
public class RequestApprovalProcessor extends CoreEventProcessor<IdmRequestDto> {
	
	public static final String PROCESSOR_NAME = "request-approval-processor";
	public static final String PROPERTY_WF = "wf";
	public static final String CHECK_RIGHT_PROPERTY = "checkRight";
	public static final String DEFAULT_WF_PROCESS_NAME = "approve-request-role";
	@Autowired
	private RequestManager manager;
	
	public RequestApprovalProcessor() {
		super(RequestEventType.EXECUTE); 
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRequestDto> process(EntityEvent<IdmRequestDto> event) {
		IdmRequestDto dto = event.getContent();
		boolean checkRight = (boolean) event.getProperties().get(CHECK_RIGHT_PROPERTY);
		//
		String wfDefinition = getConfigurationValue(PROPERTY_WF);
		if(Strings.isNullOrEmpty(wfDefinition)){
			wfDefinition = DEFAULT_WF_PROCESS_NAME;
		}
		boolean approved = manager.startApprovalProcess(dto, checkRight, event, wfDefinition);
		DefaultEventResult<IdmRequestDto> result = new DefaultEventResult<>(event, this);
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
