package eu.bcvsolutions.idm.core.model.domain;

/**
 * Role could be used for different purpose
 * 
 * @author Radek Tomiška 
 *
 */
public enum IdmRoleType {
	SYSTEM, // system role - provided by system
	BUSINESS, // role could contain technical roles
	TECHNICAL, // "leaf"
	LOGIN; // login role - for quarantine etc.
}
