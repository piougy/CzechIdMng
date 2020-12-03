package eu.bcvsolutions.idm.core.security.api.domain;

/**
 * Two factor authentication method.
 * 
 * @author Radek Tomiška 
 * @since 10.7.0
 */
public enum TwoFactorAuthenticationType {
	APPLICATION, // e.g. google authenticator
	NOTIFICATION // e.g. sms
}
