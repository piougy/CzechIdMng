package eu.bcvsolutions.idm.core.workflow.service;

import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;

import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
/**
 * Service for control workflow process instances.
 * @author svandav
 *
 */
public interface WorkflowProcessInstanceService {
	public final static String OBJECT_TYPE = "objectType";
	public final static String OBJECT_IDENTIFIER = "objectIdentifier";
	public final static String APPLICANT_USERNAME = "applicantUsername";
	public final static String APPLICANT_IDENTIFIER = "applicantIdentifier";
	/**
	 * Implementer is user really start the process (For example implementer is administrator given permission to user)
	 */
	public final static String IMPLEMENTER_USERNAME = "implementerUsername";
	
	/**
	 * Start new workflow process. To new process instance will be put variables objectType, objectIdentifier, applicant, implementer
	 * and add two involved users (applicant and implementer).
	 * 
	 * @param definitionKey
	 * @param objectType
	 * @param applicant
	 * @param objectIdentifier
	 * @param variables
	 * @return
	 */
	ProcessInstance startProcess(String definitionKey, String objectType, String applicant, String objectIdentifier,
			Map<String, Object> variables);

	/**
	 * Search process instance by filter
	 * @param filter
	 * @return
	 */
	ResourcesWrapper<WorkflowProcessInstanceDto> search(WorkflowFilterDto filter);

	/**
	 * Delete specific process instance. Only applicant or implementor can delete process instance.
	 * @param processInstanceId
	 * @param deleteReason
	 * @return
	 */
	WorkflowProcessInstanceDto delete(String processInstanceId, String deleteReason);


}
