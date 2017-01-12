package eu.bcvsolutions.idm.acc.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;

/**
 * System entity handling excerpt
 * 
 * 
 * @author Svanda
 *
 */
@Projection(name = "excerpt", types = SysSystemMapping.class)
public interface SysSystemMappingExcerpt extends AbstractDtoProjection {
	
	String getName();
	
	SystemEntityType getEntityType();
	
	SysSchemaObjectClass getObjectClass();
	
	SystemOperationType getOperationType();
}
