package eu.bcvsolutions.idm.core.model.domain;

/**
 * Role could be used for different purpose
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
public enum IdmRoleType {
	SYSTEM, // system role - can not be deleted
	BUSINESS, // role could contain technical roles
	TECHNICAL; // "leaf"
}
