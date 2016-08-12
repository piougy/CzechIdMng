package eu.bcvsolutions.idm.security.service;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import eu.bcvsolutions.idm.security.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.security.dto.IdmJwtAuthenticationDto;

/**
 * Granted authorities for users
 * 
 * @author svandav
 */
public interface GrantedAuthoritiesFactory {

	/**
	 * Returns unique set authorities by assigned active roles to given identity
	 * 
	 * @param username
	 * @return
	 */
	List<GrantedAuthority> getGrantedAuthorities(String username);

	IdmJwtAuthentication getIdmJwtAuthentication(IdmJwtAuthenticationDto dto);

	IdmJwtAuthenticationDto getIdmJwtAuthenticationDto(IdmJwtAuthentication authentication);
}
