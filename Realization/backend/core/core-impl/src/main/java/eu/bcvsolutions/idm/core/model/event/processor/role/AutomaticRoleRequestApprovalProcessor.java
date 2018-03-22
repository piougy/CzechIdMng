package eu.bcvsolutions.idm.core.model.event.processor.role;

import java.util.List;
import java.util.UUID;

import org.activiti.bpmn.model.ValuedDataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Strings;

import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleRequestDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.event.AutomaticRoleRequestEvent.AutomaticRoleRequestEventType;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessDefinitionService;

/**
 * Processor approve requested change for automatic role
 * 
 * @author svandav
 *
 */
@Component
@Description("Processor approve requested change for automatic role. By default will be started workflow with definition '"
		+ AutomaticRoleRequestApprovalProcessor.DEFAULT_WF_PROCESS_NAME + "'.")
public class AutomaticRoleRequestApprovalProcessor extends CoreEventProcessor<IdmAutomaticRoleRequestDto> {

	private static final Logger LOG = LoggerFactory.getLogger(AutomaticRoleRequestApprovalProcessor.class);

	public static final String PROCESSOR_NAME = "automatic-role-request-approval-processor";
	public static final String PROPERTY_WF = "wf";
	public static final String CHECK_RIGHT_PROPERTY = "checkRight";
	public static final String DEFAULT_WF_PROCESS_NAME = "approve-role-by-guarantee";
	public static final String SUPPORTS_AUTOMATIC_ROLE_KEY = "supportsAutomaticRole";

	private final IdmAutomaticRoleRequestService service;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private WorkflowProcessDefinitionService processDefinitionService;

	@Autowired
	public AutomaticRoleRequestApprovalProcessor(IdmAutomaticRoleRequestService service) {
		super(AutomaticRoleRequestEventType.EXECUTE);
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
	public EventResult<IdmAutomaticRoleRequestDto> process(EntityEvent<IdmAutomaticRoleRequestDto> event) {
		IdmAutomaticRoleRequestDto dto = event.getContent();
		boolean checkRight = (boolean) event.getProperties().get(CHECK_RIGHT_PROPERTY);
		// Find approval process (by role priority)
		String wfDefinition = findWfDefinition(dto);

		// If none process definition was found, then is request approved;
		if (Strings.isNullOrEmpty(wfDefinition)) {
			LOG.info("None approval process definition was found, request {} for automatic role is approved.", dto);
			return new DefaultEventResult<>(event, this);
		}

		boolean supports = this.supportsAutomaticRole(wfDefinition);
		if (!supports) {
			LOG.info(
					"Approval process definition [{}] does not supports approving for automatic role. Default approval process will be used [{}]. Automatic role request [{}]",
					wfDefinition, DEFAULT_WF_PROCESS_NAME, dto);
			wfDefinition = DEFAULT_WF_PROCESS_NAME;
		}
		boolean approved = service.startApprovalProcess(dto, checkRight, event, wfDefinition);
		DefaultEventResult<IdmAutomaticRoleRequestDto> result = new DefaultEventResult<>(event, this);
		result.setSuspended(!approved);
		return result;
	}

	private boolean supportsAutomaticRole(String wfDefinition) {
		String definitionId = processDefinitionService.getProcessDefinitionId(wfDefinition);
		List<ValuedDataObject> dataObjects = processDefinitionService.getDataObjects(definitionId);

		if (dataObjects != null) {
			ValuedDataObject supportVariable = dataObjects.stream()
					.filter(dataObject -> SUPPORTS_AUTOMATIC_ROLE_KEY.equals(dataObject.getName())).findFirst()
					.orElse(null);
			if (supportVariable != null) {
				Object value = supportVariable.getValue();
				if (value instanceof Boolean && (Boolean) value) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Find workflow definition
	 * 
	 * @param dto
	 * @return
	 */
	private String findWfDefinition(IdmAutomaticRoleRequestDto dto) {
		UUID roleId = dto.getRole();
		if (RequestOperationType.ADD == dto.getOperation()) {
			return roleService.findAssignRoleWorkflowDefinition(roleId);
		}
		if (RequestOperationType.UPDATE == dto.getOperation()) {
			return roleService.findChangeAssignRoleWorkflowDefinition(roleId);
		}
		if (RequestOperationType.REMOVE == dto.getOperation()) {
			return roleService.findUnAssignRoleWorkflowDefinition(roleId);
		}
		return DEFAULT_WF_PROCESS_NAME;
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
