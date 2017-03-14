package eu.bcvsolutions.idm.core.model.event.processor;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.event.RoleTreeNodeEvent.RoleTreeNodeEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Approve automatic role.
 * 
 * @author Radek Tomiška
 *
 */
// @Component
@Description("Approve create automatic role.")
public class RoleTreeNodeCreateApproveProcessor extends CoreEventProcessor<IdmRoleTreeNodeDto> {
	
	public static final String PROCESSOR_NAME = "role-tree-node-create-approve-processor";
	private final WorkflowProcessInstanceService workflowProcessInstanceService;
	private final SecurityService securityService;
	private final IdmIdentityService identityService;
	
	@Autowired
	public RoleTreeNodeCreateApproveProcessor(
			WorkflowProcessInstanceService workflowProcessInstanceService,
			SecurityService securityService,
			IdmIdentityService identityService) {
		super(RoleTreeNodeEventType.CREATE); // update is not supported
		//
		Assert.notNull(workflowProcessInstanceService);
		Assert.notNull(securityService);
		Assert.notNull(identityService);
		//
		this.workflowProcessInstanceService = workflowProcessInstanceService;
		this.securityService = securityService;
		this.identityService = identityService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleTreeNodeDto> process(EntityEvent<IdmRoleTreeNodeDto> event) {
		IdmRoleTreeNodeDto entity = event.getContent();
		//
		Map<String, Object> variables = new HashMap<>();
		// variables.put("roleTreeNode", entity);
		variables.put("entityEvent", event);
		IdmIdentity modifier = identityService.getByUsername(securityService.getUsername());		
		ProcessInstance processInstance = workflowProcessInstanceService.startProcess("approve-create-automatic-role",
				modifier.getClass().getSimpleName(), identityService.getNiceLabel(modifier), modifier.getId().toString(), variables);
		// TODO: auto approve ... return entity immediately
		//
		DefaultEventResult<IdmRoleTreeNodeDto> result = new DefaultEventResult<>(event, this);
		result.setSuspended(true);
		return result;
	}
	
	/**
	 * Before standard save
	 * 
	 * @return
	 */
	@Override
	public int getOrder() {
		return super.getOrder() - 1000;
	}

}
