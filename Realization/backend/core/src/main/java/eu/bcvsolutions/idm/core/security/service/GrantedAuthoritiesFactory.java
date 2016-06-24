package eu.bcvsolutions.idm.core.security.service;

import java.util.List;
import eu.bcvsolutions.idm.core.security.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.core.security.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.dto.IdmJwtAuthenticationDto;

/**
 * Granted authorities for users
 * 
 * @author svandav
 */
public interface GrantedAuthoritiesFactory {

	List<DefaultGrantedAuthority> getGrantedAuthorities(String username);

	IdmJwtAuthentication getIdmJwtAuthentication(IdmJwtAuthenticationDto dto);

	IdmJwtAuthenticationDto getIdmJwtAuthenticationDto(IdmJwtAuthentication authentication);
}
