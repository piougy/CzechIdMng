package eu.bcvsolutions.idm.acc.repository.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.core.api.repository.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

/**
 * Schema attribute excerpt
 * 
 * 
 * @author Svanda
 *
 */
@Projection(name = "excerpt", types = AccIdentityAccount.class)
public interface AccAccountExcerpt extends AbstractDtoProjection {
	
	AccAccount getAccount();

	IdmIdentity getIdentity();

	boolean isOwnership();
	
	IdmIdentityRole getIdentityRole();

}
