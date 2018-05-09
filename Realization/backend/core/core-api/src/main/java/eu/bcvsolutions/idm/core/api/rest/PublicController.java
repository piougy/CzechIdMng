package eu.bcvsolutions.idm.core.api.rest;

import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Marks controller as public - no authentication is needed.
 * Controller has to have {@link RequestMapping} annotation specified. Only values defined in this mapping will be public.
 * All controller methods with {@link RequestMapping} annotation will be public too.
 * Authorization policies can be still used.
 * Authorities annotations can be used too - a controller method can be secured additively.
 * 
 * @author Radek Tomiška
 *
 */
public interface PublicController {

}
