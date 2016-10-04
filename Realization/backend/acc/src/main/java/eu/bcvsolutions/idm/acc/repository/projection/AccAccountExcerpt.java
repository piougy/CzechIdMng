package eu.bcvsolutions.idm.acc.repository.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.core.model.repository.projection.AbstractDtoProjection;

/**
 * Account on target system excerpt
 * 
 * 
 * @author Radek Tomiška
 *
 */
@Projection(name = "excerpt", types = AccAccount.class)
public interface AccAccountExcerpt extends AbstractDtoProjection {

	String getType();
	
	SysSystem getSystem();
	
	SysSystemEntity getSystemEntity();
}
