package eu.bcvsolutions.idm.core.security.service.impl;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.spring.autoconfigure.TotpProperties;
import dev.samstevens.totp.time.TimeProvider;
import dev.samstevens.totp.util.Utils;
import eu.bcvsolutions.idm.core.api.CoreModule;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmProfileDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.service.IdmProfileService;
import eu.bcvsolutions.idm.core.api.utils.HttpFilterUtils;
import eu.bcvsolutions.idm.core.api.utils.SpinalCase;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.TwoFactorAuthenticationType;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.dto.TwoFactorRegistrationConfirmDto;
import eu.bcvsolutions.idm.core.security.api.dto.TwoFactorRegistrationResponseDto;
import eu.bcvsolutions.idm.core.security.api.exception.MustChangePasswordException;
import eu.bcvsolutions.idm.core.security.api.service.JwtAuthenticationService;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.core.security.api.service.TwoFactorAuthenticationManager;

/**
 * Additional two factor authentication method.
 * 
 * @author Radek Tomiška
 * @since 10.7.0
 */
@Service("twoFactorAuthenticationManager")
public class DefaultTwoFactorAuthenticationManager implements TwoFactorAuthenticationManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultTwoFactorAuthenticationManager.class);
	// identity services
	@Autowired private IdmIdentityService identityService;
	@Autowired private IdmPasswordService passwordService;
	@Autowired private IdmProfileService profileService;
	// notification / configuration
	@Autowired private NotificationManager notificationManager;
	@Autowired private ConfigurationService configurationService;
	// token services
	@Autowired private TokenManager tokenManager;
	@Autowired private JwtAuthenticationMapper jwtAuthenticationMapper;
	@Autowired private JwtAuthenticationService jwtAuthenticationService;
	// two-factor services
	@Autowired private SecretGenerator secretGenerator;
	@Autowired private TimeProvider timeProvider;
	@Autowired private QrDataFactory qrDataFactory;
	@Autowired private QrGenerator qrGenerator;
	@Autowired private CodeGenerator codeGenerator;
	@Autowired private CodeVerifier codeVerifier;
	@Autowired private TotpProperties props;
	
	@Override
	@Transactional
	public TwoFactorRegistrationResponseDto init(UUID identityId, TwoFactorAuthenticationType twoFactorAuthenticationType) {
		Assert.notNull(identityId, "Identity identifier is required.");
		IdmIdentityDto identity = identityService.get(identityId);
		Assert.notNull(identity, "Identity is required.");
		Assert.notNull(twoFactorAuthenticationType, "Two factor authentication method is required.");
		//
		String secret = secretGenerator.generate();
		//
		TwoFactorRegistrationResponseDto registration = new TwoFactorRegistrationResponseDto();
		registration.setVerificationSecret(secret);
		registration.setUsername(SpinalCase.format(identity.getUsername()));
		//
		// generate qr code
		if (twoFactorAuthenticationType == TwoFactorAuthenticationType.APPLICATION) {
			QrData qrcode = qrDataFactory.newBuilder()
					.label(registration.getUsername())
					.secret(secret)
					.issuer("CzechIdM") // TODO: ApplicationConfiguration
					.build();
			try {
				byte[] imageData = qrGenerator.generate(qrcode);
				String mimeType = qrGenerator.getImageMimeType();
				registration.setQrcode(Utils.getDataUriForImage(imageData, mimeType));
			} catch (Exception ex) {
				throw new ResultCodeException(CoreResultCode.TWO_FACTOR_INIT_FAILED, ex);
			}
		} else { // NOTIFICATION
			sendVerificationCode(identity, generateCode(new GuardedString(secret)));
		}
		// TODO: ApplicationConfiguration - stage development
		if ("development".equals(configurationService.getValue("idm.pub.app.stage"))) {
			LOG.warn("Development - verification code [{}].", generateCode(new GuardedString(secret)).asString());
		}
		//
		return registration;
	}
	
	@Override
	@Transactional
	public boolean confirm(UUID identityId, TwoFactorRegistrationConfirmDto registrationConfirm) {
		Assert.notNull(identityId, "Identity identifier is required.");
		Assert.notNull(registrationConfirm, "Two factor confirm request is required.");
		//
		IdmPasswordDto password = passwordService.findOneByIdentity(identityId);
		if (password == null) { 
			throw new EntityNotFoundException(IdmIdentityDto.class, identityId);
		}
		//
		GuardedString verificationSecret = registrationConfirm.getVerificationSecret();
		GuardedString verificationCode = registrationConfirm.getVerificationCode();
		//
		if (!verifyCode(verificationSecret, verificationCode)) {
			throw new ResultCodeException(CoreResultCode.TWO_FACTOR_VERIFICATION_CODE_FAILED);
		}
		//
		password.setVerificationSecret(verificationSecret.asString());
		passwordService.save(password);
		//
		IdmProfileDto profile = profileService.findOrCreateByIdentity(identityId);
		profile.setTwoFactorAuthenticationType(registrationConfirm.getTwoFactorAuthenticationType());
		profileService.save(profile);
		//
		return true;
	}
	
	@Override
	public GuardedString generateCode(UUID identityId) {
		IdmPasswordDto password = passwordService.findOneByIdentity(identityId);
		if (password == null) { 
			throw new EntityNotFoundException(IdmIdentityDto.class, identityId);
		}
		//
		return generateCode(new GuardedString(password.getVerificationSecret()));
	}
	
	@Override
	public GuardedString generateCode(GuardedString verificationSecret) {
		Assert.notNull(verificationSecret, "Verification secret is required.");
		//
		long currentBucket = Math.floorDiv(timeProvider.getTime(), props.getTime().getPeriod());
		try {
			return new GuardedString(codeGenerator.generate(verificationSecret.asString(), currentBucket));
		} catch (CodeGenerationException ex) {
			throw new ResultCodeException(CoreResultCode.TWO_FACTOR_GENERATE_CODE_FAILED, ex);
		}
	}
	
	@Override
	public boolean verifyCode(UUID identityId, GuardedString verificationCode) {
		IdmPasswordDto password = passwordService.findOneByIdentity(identityId);
		if (password == null) { 
			throw new EntityNotFoundException(IdmIdentityDto.class, identityId);
		}
		//
		return verifyCode(password, verificationCode);
	}
	
	@Override
	public boolean verifyCode(GuardedString verificationSecret, GuardedString verificationCode) {
		Assert.notNull(verificationSecret, "Verification secret is required.");
		Assert.notNull(verificationCode, "Verification code is required.");
		//
		return codeVerifier.isValidCode(verificationSecret.asString(), verificationCode.asString());
	}
	
	@Override
	public TwoFactorAuthenticationType getTwoFactorAuthenticationType(UUID identityId) {
		// check two factor authentication is enabled
		IdmProfileDto profile = profileService.findOneByIdentity(identityId);
		if (profile == null) {
			return null;
		}
		return profile.getTwoFactorAuthenticationType();
	}
	
	@Override
	@Transactional
	public boolean requireTwoFactorAuthentication(UUID identityId, UUID tokenId) {
		// check two factor authentication is enabled
		TwoFactorAuthenticationType twoFactorAuthenticationType = getTwoFactorAuthenticationType(identityId);
		if (twoFactorAuthenticationType == null) {
			return false;
		}
		//
		IdmTokenDto token = tokenManager.getToken(tokenId);
		if (token.isSecretVerified()) {
			// token was already verified
			return false;
		}
		//
		if (TwoFactorAuthenticationType.NOTIFICATION == twoFactorAuthenticationType) {
			IdmPasswordDto password = passwordService.findOneByIdentity(identityId);
			if (password == null) { 
				throw new EntityNotFoundException(IdmIdentityDto.class, identityId);
			}
			sendVerificationCode(
					identityService.get(identityId),
					generateCode(new GuardedString(password.getVerificationSecret()))
			);
		}
		// TODO: ApplicationConfiguration - stage development
		if ("development".equals(configurationService.getValue("idm.pub.app.stage"))) {
			LOG.warn("Development - verification code [{}].", 
					generateCode(new GuardedString(passwordService.findOneByIdentity(identityId).getVerificationSecret())).asString());
		}
		//
		return true;
	}
	
	@Override
	@Transactional
	public LoginDto authenticate(LoginDto loginTwoFactorRequestDto) {
		Assert.notNull(loginTwoFactorRequestDto, "Login request is required.");
		//
		Optional<Jwt> jwt = HttpFilterUtils.parseToken(loginTwoFactorRequestDto.getToken());
		if (!jwt.isPresent()) {
			throw new ResultCodeException(CoreResultCode.AUTH_FAILED, "Verification code must be filled");
		}
		HttpFilterUtils.verifyToken(jwt.get(), jwtAuthenticationMapper.getVerifier());
		// authentication dto from request
		IdmJwtAuthenticationDto claims = null;
		try {
			claims = jwtAuthenticationMapper.getClaims(jwt.get());
		} catch (IOException ex) {
			throw new ResultCodeException(CoreResultCode.TOKEN_READ_FAILED, ex);
		}
		// check expiration for token given in header
		// we need to check expiration, before current (automatically prolonged) token is used by mapper
		if (claims.getExpiration() != null && claims.getExpiration().isBefore(ZonedDateTime.now())) {
			throw new ResultCodeException(CoreResultCode.AUTH_EXPIRED);
		}
		UUID identityId = claims.getCurrentIdentityId();
		IdmIdentityDto identity = identityService.get(identityId);
		if (identity == null) { 
			throw new EntityNotFoundException(IdmIdentityDto.class, identityId);
		}
		IdmPasswordDto password = passwordService.findOneByIdentity(identityId);
		if (password == null) { 
			throw new EntityNotFoundException(IdmPasswordDto.class, identityId);
		}
		if (!verifyCode(password, loginTwoFactorRequestDto.getPassword())) {
			throw new ResultCodeException(CoreResultCode.TWO_FACTOR_VERIFICATION_CODE_FAILED);
		}
		//
		if (password.isMustChange() && !loginTwoFactorRequestDto.isSkipMustChange()) {
			throw new MustChangePasswordException(claims.getCurrentUsername());
		}
        // set token verified 
        IdmTokenDto token = tokenManager.getToken(claims.getId());
        token.setSecretVerified(true);
        // and login - new login dto new to be constructed to preserve original login metadata
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername(claims.getCurrentUsername());
        loginDto.setAuthenticationModule(claims.getFromModule());
        //
        return jwtAuthenticationService.createJwtAuthenticationAndAuthenticate(
        		loginDto, 
        		identity,
        		token
        );
	}
	
	private boolean verifyCode(IdmPasswordDto password, GuardedString verificationCode) {
		Assert.notNull(password, "Identity IdM password is required.");
		String verificationSecret = password.getVerificationSecret();
		Assert.hasLength(verificationSecret, "Password does not have verification secret initialized.");
		//
		return verifyCode(new GuardedString(verificationSecret), verificationCode);
	}
	
	private void sendVerificationCode(IdmIdentityDto identity, GuardedString verificationCode) {
		notificationManager.send(
				CoreModule.TOPIC_TWO_FACTOR_VERIFICATION_CODE,
				new IdmMessageDto
						.Builder()
						.setLevel(NotificationLevel.SUCCESS)
						.addParameter("verificationCode", verificationCode)
						.addParameter("identity", identity)
						.build(), 
				identity);
	}
}
