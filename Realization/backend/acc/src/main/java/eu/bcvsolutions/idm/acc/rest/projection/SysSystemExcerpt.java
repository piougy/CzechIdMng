package eu.bcvsolutions.idm.acc.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.entity.SysConnectorKey;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;

/**
 * Target System excerpt
 * 
 * 
 * @author Radek Tomiška
 *
 */
@Projection(name = "excerpt", types = SysSystem.class)
public interface SysSystemExcerpt extends AbstractDtoProjection {

	String getName();
	
	String getDescription();
	
	boolean isDisabled();
	
	boolean isVirtual();
	
	SysConnectorKey getConnectorKey();
}
