package eu.bcvsolutions.idm.core.security.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.jwt.crypto.sign.SignerVerifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.dto.IdentityDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.dto.DefaultGrantedAuthorityDto;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.domain.DefaultGrantedAuthority;

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
		SignerVerifier verifier = new MacSigner(getSecret().asString());
		String decoded = JwtHelper.decodeAndVerify(token, verifier).getClaims();
		return fromDto(mapper.readValue(decoded, IdmJwtAuthenticationDto.class));
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
				new IdentityDto(dto.getCurrentIdentityId(), dto.getCurrentUsername()),
				new IdentityDto(dto.getOriginaIdentityId(), dto.getOriginalUsername()), 
				dto.getExpiration(), 
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
	@SuppressWarnings("unchecked")
	public IdmJwtAuthenticationDto toDto(IdmJwtAuthentication authentication) {
		Assert.notNull(authentication);
		//
		IdmJwtAuthenticationDto authenticationDto = new IdmJwtAuthenticationDto();
		authenticationDto.setCurrentUsername(authentication.getCurrentUsername());
		authenticationDto.setCurrentIdentityId(authentication.getCurrentIdentity() == null ? null : authentication.getCurrentIdentity().getId());
		authenticationDto.setOriginalUsername(authentication.getOriginalUsername());
		authenticationDto.setOriginaIdentityId(authentication.getOriginalIdentity() == null ? null : authentication.getOriginalIdentity().getId());
		authenticationDto.setExpiration(authentication.getExpiration());
		authenticationDto.setFromModule(authentication.getFromModule());
		Collection<DefaultGrantedAuthority> authorities = (Collection<DefaultGrantedAuthority>) authentication
				.getAuthorities();
		List<DefaultGrantedAuthorityDto> grantedAuthorities = new ArrayList<>();
		if (authorities != null) {
			for (DefaultGrantedAuthority a : authorities) {
				grantedAuthorities.add(new DefaultGrantedAuthorityDto(a.getAuthority()));
			}
		}
		authenticationDto.setAuthorities(grantedAuthorities);
		return authenticationDto;
	}
	
	
}
