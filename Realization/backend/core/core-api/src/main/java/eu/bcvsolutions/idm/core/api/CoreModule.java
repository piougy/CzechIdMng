package eu.bcvsolutions.idm.core.api;

import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;

/**
 * Core module descriptor - public interface.
 * 
 * @author Radek Tomiška
 * @since 10.5.0 - module descriptor in core-impl package should not be used
 */
public interface CoreModule extends ModuleDescriptor {

	String MODULE_ID = "core";
	//
	String TOPIC_CHANGE_IDENTITY_ROLES = String.format("%s:changeIdentityRole", MODULE_ID);
	String TOPIC_CHANGE_IDENTITY_ROLES_IMPLEMENTER = String.format("%s:changeIdentityRoleImplementer", MODULE_ID);
	String TOPIC_REQUEST_REALIZED_APPLICANT = String.format("%s:roleRequestRealizedApplicant", MODULE_ID);
	String TOPIC_REQUEST_REALIZED_IMPLEMENTER = String.format("%s:roleRequestRealizedImplementer", MODULE_ID);
	String TOPIC_DISAPPROVE_IDENTITY_ROLES = String.format("%s:disapproveIdentityRole", MODULE_ID);
	String TOPIC_DISAPPROVE_IDENTITY_ROLES_IMPLEMENTER = String.format("%s:disapproveIdentityRoleImplementer", MODULE_ID);
	String TOPIC_RETURN_REQUEST_IDENTITY_ROLES = String.format("%s:returnRequestIdentityRole", MODULE_ID);		
	String TOPIC_RETURN_REQUEST_IDENTITY_ROLES_IMPLEMENTER = String.format("%s:returnRequestIdentityRoleImplementer", MODULE_ID);
	String TOPIC_WF_TASK_CREATED = String.format("%s:wfTaskCreated", MODULE_ID);
	@Deprecated // @since 10.5.0 - not used in product - use TOPIC_WF_TASK_CREATED
	String TOPIC_WF_TASK_ASSIGNED = String.format("%s:wfTaskAssigned", MODULE_ID);
	String TOPIC_PASSWORD_EXPIRATION_WARNING = String.format("%s:passwordExpirationWarning", MODULE_ID);
	String TOPIC_PASSWORD_EXPIRED = String.format("%s:passwordExpired", MODULE_ID);
	String TOPIC_IDENTITY_MONITORED_CHANGED_FIELDS = String.format("%s:identityMonitoredFieldsChanged", MODULE_ID);
	String TOPIC_PASSWORD_CHANGED = String.format("%s:passwordChanged", MODULE_ID);
	String TOPIC_PASSWORD_SET = String.format("%s:passwordSet", MODULE_ID);
	String TOPIC_EVENT = String.format("%s:event", MODULE_ID);
	String TOPIC_LOGIN_BLOCKED = String.format("%s:loginBlocked", MODULE_ID);
	String TOPIC_BULK_ACTION_END = String.format("%s:bulkActionEnd", MODULE_ID);
	String TOPIC_DELEGATION_CREATED_TO_DELEGATE = String.format("%s:delegationCreatedToDelegate", MODULE_ID);
	String TOPIC_DELEGATION_CREATED_TO_DELEGATOR = String.format("%s:delegationCreatedToDelegator", MODULE_ID);
	String TOPIC_DELEGATION_DELETED_TO_DELEGATE = String.format("%s:delegationDeletedToDelegate", MODULE_ID);
	String TOPIC_DELEGATION_DELETED_TO_DELEGATOR = String.format("%s:delegationDeletedToDelegator", MODULE_ID);
	String TOPIC_DELEGATION_INSTANCE_CREATED_TO_DELEGATE = String.format("%s:delegationInstanceCreatedToDelegate", MODULE_ID);
	String TOPIC_TWO_FACTOR_VERIFICATION_CODE = String.format("%s:twoFactorVerificationCode", MODULE_ID);
	
}
