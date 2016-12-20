package eu.bcvsolutions.idm.acc.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;

/**
 * System entity handling excerpt
 * 
 * 
 * @author Svanda
 *
 */
@Projection(name = "excerpt", types = SysSystemEntityHandling.class)
public interface SysSystemEntityHandlingExcerpt extends AbstractDtoProjection {
	
	SystemEntityType getEntityType();
	
	SystemOperationType getOperationType();
	
	SysSystem getSystem();

}
