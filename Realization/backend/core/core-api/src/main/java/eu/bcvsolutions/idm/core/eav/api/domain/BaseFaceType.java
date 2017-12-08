package eu.bcvsolutions.idm.core.eav.api.domain;

/**
 * Basic core faces types. Default face type is resolved by attribute's {@link PersistentType}. 
 * 
 * @author Radek Tomiška
 *
 */
public interface BaseFaceType {
  
	final static String TEXTAREA = "TEXTAREA";
	final static String RICHTEXTAREA = "RICHTEXTAREA";
	//
	final static String CURRENCY = "CURRENCY";
	//
	final static String IDENTITY_SELECT = "IDENTITY-SELECT";
	final static String ROLE_SELECT = "ROLE-SELECT";
	//
	final static String BOOLEAN_SELECT = "BOOLEAN-SELECT";
}
