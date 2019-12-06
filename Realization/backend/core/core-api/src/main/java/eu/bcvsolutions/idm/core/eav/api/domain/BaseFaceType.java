package eu.bcvsolutions.idm.core.eav.api.domain;

/**
 * Basic core faces types. Default face type is resolved by attribute's {@link PersistentType}. 
 * 
 * @author Radek Tomiška
 *
 */
public interface BaseFaceType {
  
	String TEXTAREA = "TEXTAREA";
	String RICHTEXTAREA = "RICHTEXTAREA";
	//
	String CURRENCY = "CURRENCY";
	//
	String IDENTITY_SELECT = "IDENTITY-SELECT";
	String ROLE_SELECT = "ROLE-SELECT";
	String ROLE_CAN_BE_REQUESTED_SELECT = "ROLE-CAN-BE-REQUESTED-SELECT";
	String FORM_DEFINITION_SELECT = "FORM-DEFINITION-SELECT";
	String TREE_NODE_SELECT = "TREE-NODE-SELECT";
	String ROLE_CATALOGUE_SELECT = "ROLE-CATALOGUE-SELECT";
	String CODE_LIST_SELECT = "CODE-LIST-SELECT";
	String AUTOMATIC_ROLE_TREE_SELECT = "AUTOMATIC-ROLE-TREE-SELECT";
	String AUTOMATIC_ROLE_ATTRIBUTE_SELECT = "AUTOMATIC-ROLE-ATTRIBUTE-SELECT";
	String WORKFLOW_DEFINITION_SELECT = "WORKFLOW-DEFINITION-SELECT";
	//
	String BOOLEAN_SELECT = "BOOLEAN-SELECT";
	//
	String OPERATION_STATE_ENUM = "OPERATION-STATE-ENUM"; // OperationState ~ OperationStateEnum
}
