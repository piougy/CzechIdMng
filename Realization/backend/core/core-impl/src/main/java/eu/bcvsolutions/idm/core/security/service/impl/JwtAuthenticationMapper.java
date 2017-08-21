package eu.bcvsolutions.idm.core.security.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.jwt.crypto.sign.SignerVerifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.security.api.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.dto.DefaultGrantedAuthorityDto;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;

/**
 * Reads authentication from token and provides conversions from / to dto nd to token. 
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class JwtAuthenticationMapper {

	public static final String AUTHENTICATION_TOKEN_NAME = "CIDMST";
	public static final String PROPERTY_SECRET_TOKEN = "idm.sec.security.jwt.secret.token";
	public static final String DEFAULT_SECRET_TOKEN = "idmSecret";
	//
	private final ObjectMapper mapper;
	private final ConfigurationService configurationService;

	@Autowired
	public JwtAuthenticationMapper(
			@Qualifier("objectMapper") ObjectMapper mapper,
			ConfigurationService configurationService) {
		Assert.notNull(mapper);
		Assert.notNull(configurationService);
		//
		this.mapper = mapper;
		this.configurationService = configurationService;
	}
	
	/**
	 * Reads {@link IdmJwtAuthentication} from given token
	 * 
	 * @param token
	 * @return
	 * @throws IOException
	 */
	public IdmJwtAuthentication readToken(String token) throws IOException {
		if (StringUtils.isEmpty(token)) {
			return null;
		}
		//
		SignerVerifier verifier = getVerifier();
		String decoded = JwtHelper.decodeAndVerify(token, verifier).getClaims();
		return fromDto(mapper.readValue(decoded, IdmJwtAuthenticationDto.class));
	}
	
	/**
	 * Return IdM OAuth token verifier.
	 * @return
	 */
	public SignerVerifier getVerifier() {
		return new MacSigner(getSecret().asString());
	}
	
	/**
	 * Writes authentication to token
	 * 
	 * @param authentication
	 * @return
	 * @throws IOException
	 */
	public String writeToken(IdmJwtAuthentication authentication) throws IOException {
		Assert.notNull(authentication);
		//
		return writeToken(toDto(authentication));
	}
	
	/**
	 * Writes authentication dto to token
	 * 
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	public String writeToken(IdmJwtAuthenticationDto dto) throws IOException {
		Assert.notNull(dto);
		//
		String authenticationJson = mapper.writeValueAsString(dto);
		return JwtHelper.encode(authenticationJson, new MacSigner(getSecret().asString())).getEncoded();
	}
	
	/**
	 * Reads secret from configuration
	 * 
	 * @return
	 */
	private GuardedString getSecret() {
		return configurationService.getGuardedValue(PROPERTY_SECRET_TOKEN, DEFAULT_SECRET_TOKEN);
	}
	
	/**
	 * Converts dto to authentication.
	 * 
	 * @param dto
	 * @return
	 */
	public IdmJwtAuthentication fromDto(IdmJwtAuthenticationDto dto) {
		Assert.notNull(dto);
		//
		Collection<DefaultGrantedAuthorityDto> authorities = dto.getAuthorities();
		List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
		if (authorities != null) {
			for (DefaultGrantedAuthorityDto a : authorities) {
				grantedAuthorities.add(new DefaultGrantedAuthority(a.getAuthority()));
			}
		}
		IdmJwtAuthentication authentication = new IdmJwtAuthentication(
				new IdmIdentityDto(dto.getCurrentIdentityId(), dto.getCurrentUsername()),
				new IdmIdentityDto(dto.getOriginalIdentityId(), dto.getOriginalUsername()), 
				dto.getExpiration(),
				dto.getIssuedAt(),
				grantedAuthorities,
				dto.getFromModule());
		return authentication;
	}
	
	/**
	 * Converts authentication.
	 * 
	 * @param authentication to dto
	 * @return
	 */
	public IdmJwtAuthenticationDto toDto(IdmJwtAuthentication authentication) {
		Assert.notNull(authentication);
		//
		IdmJwtAuthenticationDto authenticationDto = new IdmJwtAuthenticationDto();
		authenticationDto.setCurrentUsername(authentication.getCurrentUsername());
		authenticationDto.setCurrentIdentityId(getIdentityId(authentication.getCurrentIdentity()));
		authenticationDto.setOriginalUsername(authentication.getOriginalUsername());
		authenticationDto.setOriginalIdentityId(getIdentityId(authentication.getOriginalIdentity()));
		authenticationDto.setExpiration(authentication.getExpiration());
		authenticationDto.setFromModule(authentication.getFromModule());
		authenticationDto.setIssuedAt(DateTime.now());
		authenticationDto.setAuthorities(getDtoAuthorities(authentication));
		return authenticationDto;
	}
	
	/**
	 * 
	 * @param authentication
	 * @return
	 * @deprecated use {@link #getDtoAuthorities(Authentication)}
	 */
	@Deprecated
	public List<DefaultGrantedAuthorityDto> getDTOAuthorities(Authentication authentication) {
		return getDtoAuthorities(authentication);
	}

	/**
	 * 
	 * 
	 * @param authentication
	 * @return
	 * @deprecated will be private
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public List<DefaultGrantedAuthorityDto> getDtoAuthorities(Authentication authentication) {
		Collection<DefaultGrantedAuthority> authorities = (Collection<DefaultGrantedAuthority>) authentication
				.getAuthorities();
		List<DefaultGrantedAuthorityDto> grantedAuthorities = new ArrayList<>();
		if (authorities != null) {
			for (DefaultGrantedAuthority a : authorities) {
				grantedAuthorities.add(new DefaultGrantedAuthorityDto(a.getAuthority()));
			}
		}
		return grantedAuthorities;
	}
	
	public IdmJwtAuthenticationDto getClaims(Jwt jwt) throws IOException {
		return mapper.readValue(jwt.getClaims(), IdmJwtAuthenticationDto.class);
	}
	
	private UUID getIdentityId(IdmIdentityDto dto) {
		return dto == null ? null : dto.getId();
	}
	
}
