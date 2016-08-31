package eu.bcvsolutions.idm.core.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.Controller;

import eu.bcvsolutions.idm.core.model.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;

/**
 * Adds custom rest methods for IdmIdentity resource
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmIdentityController extends IdmRevisionController  {
	
	static final String ENDPOINT_NAME = "identities";
	
	/**
	 * Changes identity password. Could be public, because previous password is required.
	 * 
	 * @param identityId
	 * @param passwordChangeDto
	 * @return
	 */
	ResponseEntity<Void> passwordChange(String identityId, PasswordChangeDto passwordChangeDto);
	
	/**
	 * Returns given identity's granted authorities
	 * 
	 * @param identityId
	 * @return list of granted authorities
	 */
	List<? extends GrantedAuthority> getGrantedAuthotrities(String identityId);
	
	/**
	 * Change given identity's permissions (assigned roles)
	 * @param identityId
	 * @return Instance of workflow user task, where applicant can fill his change permission request
	 */
	ResponseEntity<ResourceWrapper<WorkflowTaskInstanceDto>> changePermissions(String identityId);

}
