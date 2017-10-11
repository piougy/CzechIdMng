package eu.bcvsolutions.idm.core.api.domain;

/**
 * Password change type (this is configurable from application properties)
 *	DISABLED - password change is disabled, only admin can change password
 *	ALL_ONLY - users can change passwords only for all accounts
 *	CUSTOM - users can choose for which accounts change password
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public enum PasswordChangeType {
	DISABLED, 
	ALL_ONLY, 
	CUSTOM
}
