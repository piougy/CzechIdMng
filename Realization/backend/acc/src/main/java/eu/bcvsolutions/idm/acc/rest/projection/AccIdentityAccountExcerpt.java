package eu.bcvsolutions.idm.acc.rest.projection;

import java.util.List;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;

/**
 * Account excerpt
 * 
 * 
 * @author Svanda
 *
 */
@Projection(name = "excerpt", types = AccAccount.class)
public interface AccIdentityAccountExcerpt extends AbstractDtoProjection {
	
	AccountType getAccountType();

	SysSystem getSystem();

	SysSystemEntity getSystemEntity();
	
	List<AccIdentityAccount> getIdentityAccounts();
	
	String getUid();

}
