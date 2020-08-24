package eu.bcvsolutions.idm.core.api.domain;

/**
 * Role could be used for different purpose.
 * 
 * @author Radek Tomiška 
 *
 */
public enum RoleType {
	SYSTEM, // system role - provided by product CzechIdM
	@Deprecated // @since 10.5.0 - SYSTEM role is used only
	BUSINESS, // role could contain technical roles
	@Deprecated // @since 10.5.0 - SYSTEM role is used only
	TECHNICAL, // "leaf"
	@Deprecated // @since 10.5.0 - SYSTEM role is used only
	LOGIN; // login role - for quarantine etc.
}
