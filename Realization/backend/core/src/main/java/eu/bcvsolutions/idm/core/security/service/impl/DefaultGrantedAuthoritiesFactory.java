package eu.bcvsolutions.idm.core.security.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.security.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.core.security.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.dto.DefaultGrantedAuthorityDto;
import eu.bcvsolutions.idm.core.security.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.core.security.service.GrantedAuthoritiesFactory;

/**
 * @author svandav
 */
@Component
public class DefaultGrantedAuthoritiesFactory implements GrantedAuthoritiesFactory {

	@Autowired
	private IdmIdentityRepository idmIdentityRepository;

	@Override
	public List<DefaultGrantedAuthority> getGrantedAuthorities(String username) {

		List<DefaultGrantedAuthority> grantedAuthorities = new ArrayList<>();

		IdmIdentity identity = idmIdentityRepository.findOneByUsername(username);
		if (identity == null) {
			throw new IdmAuthenticationException("Identity " + username + " not found!");
		}

		List<IdmIdentityRole> roles = identity.getRoles();
		for (IdmIdentityRole ir : roles) {
			grantedAuthorities.add(new DefaultGrantedAuthority(ir.getRole().getName()));
		}

		return grantedAuthorities;
	}

	@Override
	public IdmJwtAuthentication getIdmJwtAuthentication(IdmJwtAuthenticationDto dto) {
		Collection<DefaultGrantedAuthorityDto> authorities = dto.getAuthorities();
		List<DefaultGrantedAuthority> grantedAuthorities = new ArrayList<>();
		if (authorities != null) {
			for (DefaultGrantedAuthorityDto a : authorities) {
				grantedAuthorities.add(new DefaultGrantedAuthority(a.getRoleName()));
			}
		}
		IdmJwtAuthentication authentication = new IdmJwtAuthentication(dto.getCurrentUsername(),
				dto.getOriginalUsername(), dto.getExpiration(), grantedAuthorities);
		return authentication;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IdmJwtAuthenticationDto getIdmJwtAuthenticationDto(IdmJwtAuthentication authentication) {
		IdmJwtAuthenticationDto authenticationDto = new IdmJwtAuthenticationDto();
		authenticationDto.setCurrentUsername(authentication.getCurrentUsername());
		authenticationDto.setOriginalUsername(authentication.getOriginalUsername());
		authenticationDto.setExpiration(authentication.getExpiration());
		if (authentication.getAuthorities() instanceof Collection<?>) {
			Collection<DefaultGrantedAuthority> authorities = (Collection<DefaultGrantedAuthority>) authentication
					.getAuthorities();
			List<DefaultGrantedAuthorityDto> grantedAuthorities = new ArrayList<>();
			if (authorities != null) {
				for (DefaultGrantedAuthority a : authorities) {
					grantedAuthorities.add(new DefaultGrantedAuthorityDto(a.getRoleName(), a.getAuthority()));
				}
			}
			authenticationDto.setAuthorities(grantedAuthorities);
		}
		return authenticationDto;
	}
}
