package eu.bcvsolutions.idm.acc.repository.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.core.api.repository.projection.AbstractDtoProjection;

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

}
