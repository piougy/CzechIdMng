package eu.bcvsolutions.idm.core.security.api.service;

import java.util.UUID;

import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.TwoFactorAuthenticationType;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.dto.TwoFactorRegistrationConfirmDto;
import eu.bcvsolutions.idm.core.security.api.dto.TwoFactorRegistrationResponseDto;

/**
 * Additional two factor authentication method.
 * 
 * @author Radek Tomiška
 * @since 10.7.0
 */
public interface TwoFactorAuthenticationManager {

	/**
	 * Initialize two-factor authentication for selected identity.
	 * Send notification with init code if needed.
	 * 
	 * @param identityId request enable two-factor authentication for selected identity
	 * @param twoFactorAuthenticationType two-factor authentication method
	 * @return confirm information
	 */
	TwoFactorRegistrationResponseDto init(UUID identityId, TwoFactorAuthenticationType twoFactorAuthenticationType);
	
	/**
	 * Confirm (set) two-factor authentication for selected identity.
	 * 
	 * @param identityId enable two-factor authentication for selected identity
	 * @param registrationConfirm confirm information
	 * @return True => confirm succeed + two-factor authentication is enabled. Exception is thrown otherwise.
	 */
	boolean confirm(UUID identityId, TwoFactorRegistrationConfirmDto registrationConfirm);
	
	/**
	 * Generate two factor authentication code.
	 * 
	 * @param identityId for selected identity
	 * @return code
	 */
	GuardedString generateCode(UUID identityId);
	
	/**
	 * Generate two factor authentication code.
	 * 
	 * @param verificationSecret verification secret
	 * @return code
	 */
	GuardedString generateCode(GuardedString verificationSecret);
	
	/**
	 * Verify two factor authentication code.
	 * 
	 * @param identityId for selected identity
	 * @param verificationCode 6-digits code
	 * @return true - valid 
	 */
	boolean verifyCode(UUID identityId, GuardedString verificationCode);
	
	/**
	 * Verify two factor authentication code.
	 * 
	 * @param identityId for selected identity
	 * @param verificationCode 6-digits code
	 * @return true - valid 
	 */
	boolean verifyCode(GuardedString verificationSecret, GuardedString verificationCode);
	
	/**
	 * Get configured two factor authentication for given identity.
	 * 
	 * @param identityId for selected identity
	 * @return configured two factor authentication method or {@code null} => not configured.
	 */
	TwoFactorAuthenticationType getTwoFactorAuthenticationType(UUID identityId);
	
	/**
	 * Check two factor authentication is required for given identity and token
	 * and send code to identity by configured two factor authentication method
	 * 
	 * @param identityId for selected identity
	 * @param tokenId used token
	 * @return true - two factor authentication is required and token was not verified already
	 */
	boolean requireTwoFactorAuthentication(UUID identityId, UUID tokenId);
	
	/**
	 * Process two factor authentication.
	 * 
	 * @param loginDto
	 * @see AuthenticationManager#authenticate(LoginDto)
	 */
	LoginDto authenticate(LoginDto loginDto);
}
