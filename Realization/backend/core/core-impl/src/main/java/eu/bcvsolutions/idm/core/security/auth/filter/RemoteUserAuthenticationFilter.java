package eu.bcvsolutions.idm.core.security.auth.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Authentication filter which enables login to IdM by a request remote user.
 *
 * @author Radek Tomiška
 *
 */
@Order(25)
@Component(RemoteUserAuthenticationFilter.FILTER_NAME)
@Enabled(module = CoreModuleDescriptor.MODULE_ID)
public class RemoteUserAuthenticationFilter extends SsoIdmAuthenticationFilter {

	private static final Logger LOG = LoggerFactory.getLogger(RemoteUserAuthenticationFilter.class);
	//
	public static final String FILTER_NAME = "core-remote-user-authentication-filter";

	@Override
	public String getName() {
		return FILTER_NAME;
	}

	@Override
	public boolean authorize(HttpServletRequest request, HttpServletResponse response) {
		String remoteUser = request.getRemoteUser();
		//
		LOG.debug("Starting filter authorization, value of the remote user is: [{}]", remoteUser);
		//
		return super.authorize(remoteUser, request, response);
	}

	@Override
	public boolean authorize(String token, HttpServletRequest request, HttpServletResponse response) {
		// prevent to authorize by header
		return false;
	}

	@Override
	protected boolean isSsoDisabledForIdentity(IdmIdentityDto identity) {
		// all identities can be authenticated thru remote user
		return false;
	}
}
