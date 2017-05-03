package eu.bcvsolutions.idm.core.security.api.domain;

import eu.bcvsolutions.idm.core.security.api.authentication.Authenticator;

/**
 * Authentication response from {@link Authenticator}
 * <p>
 *  - requisite		- Failure are caused immediately exit of all authentication chain and throw exception
 *  - required		- Failure are caused throw exception after complete all authentication chain
 *  - sufficient	- Successful validation immediately exit all authentication and identity is authenticate
 *  - optional		- If is authenticator alone decides whether the user authenticate, if there is more authenticator result will be ignored
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */

public enum AuthenticationResponseEnum {
	REQUISITE, REQUIRED, SUFFICIENT, OPTIONAL
}
