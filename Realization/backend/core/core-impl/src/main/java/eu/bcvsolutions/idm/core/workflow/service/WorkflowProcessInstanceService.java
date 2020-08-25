package eu.bcvsolutions.idm.core.workflow.service;

import java.util.Map;
import java.util.Set;

import org.activiti.engine.runtime.ProcessInstance;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
/**
 * Service for control workflow process instances.
 * 
 * @author svandav
 *
 */
public interface WorkflowProcessInstanceService extends ReadWriteDtoService<WorkflowProcessInstanceDto, WorkflowFilterDto> {
	
	String OBJECT_TYPE = "objectType";
	String OBJECT_IDENTIFIER = "objectIdentifier";
	String APPLICANT_USERNAME = "applicantUsername";
	String APPLICANT_IDENTIFIER = "applicantIdentifier";
	String ACTIVITI_SKIP_EXPRESSION_ENABLED = "_ACTIVITI_SKIP_EXPRESSION_ENABLED";
	String PROCESS_INSTANCE_ID = "processInstanceId"; // we need processInstanceId have accessible in subprocess (set in wf start listener)
	
	/**
	 * Implementer is user really start the process (For example implementer is administrator given permission to user)
	 */
	String IMPLEMENTER_IDENTIFIER = "implementerIdentifier";
	
	/**
	 * Original implementer is user before switch (For example original implementer is administrator switched to another user).
	 * 
	 * @since 10.5.0 - switch user feature implemented
	 */
	String ORIGINAL_IMPLEMENTER_IDENTIFIER = "originalImplementerIdentifier";
	
	/**
	 * Name of the workflow variable which stores the operation result
	 * 
	 * of type {@link OperationResult}.
	 */
	String VARIABLE_OPERATION_RESULT = "operationResult";
	
	/**
	 * Name of the workflow variable which stores the input dto
	 * 
	 * of type {@link BaseDto}.
	 */
	String VARIABLE_DTO = "dto";
	
	/**
	 * Start new workflow process. To new process instance will be put variables objectType, objectIdentifier, applicant, implementer
	 * and add two involved users (applicant and implementer).
	 * 
	 * @param definitionKey
	 * @param objectType - Mostly type of applicant 
	 * @param applicant  - User readable applicant name
	 * @param objectIdentifier - Mostly identifier of applicant
	 * @param variables
	 * @return
	 */
	ProcessInstance startProcess(String definitionKey, String objectType, String applicant, String objectIdentifier,
			Map<String, Object> variables);

	/**
	 * Delete specific process instance. Only applicant or implementor can delete process instance.
	 * @param processInstanceId
	 * @param deleteReason
	 * @return
	 */
	WorkflowProcessInstanceDto delete(String processInstanceId, String deleteReason);

	/**
	 * Search process instance by ID process instance
	 * @param processInstanceId
	 * @return
	 */
	WorkflowProcessInstanceDto get(String processInstanceId);
	
	/**
	 * Search process instance by ID process instance and check permission
	 * 
	 * @param processInstanceId
	 * @param checkRight
	 * @return
	 */
	WorkflowProcessInstanceDto get(String processInstanceId, boolean checkRight);

	/**
	 * Return unique set of all approvers for all subprocess of given parent process id in parameters.
	 *
	 * @param processInstaceId
	 * @return
	 */
	Set<IdmIdentityDto> getApproversForSubprocess(String processInstaceId);

	/**
	 * Return unique set of all approvers for given process id in parameters. This method also include approvers for subprocess.
	 *
	 * @param processInstaceId
	 * @return
	 */
	Set<IdmIdentityDto> getApproversForProcess(String processInstaceId);
}
