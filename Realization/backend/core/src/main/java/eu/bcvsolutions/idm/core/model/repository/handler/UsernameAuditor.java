package eu.bcvsolutions.idm.core.model.repository.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.bcvsolutions.idm.core.security.service.SecurityService;

/**
 * Retrieve auditor for auditable entity
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 */
public class UsernameAuditor implements AuditorAware<String> {
	
	@Autowired
	private SecurityService securityService;

	@Override
	public String getCurrentAuditor() {
	    String username = securityService.getUsername();
	    return StringUtils.isEmpty(username) ? "[GUEST]" : username;
	}

}