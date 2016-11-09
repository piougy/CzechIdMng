package eu.bcvsolutions.idm.acc.repository.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.core.api.repository.projection.AbstractDtoProjection;

/**
 * Schema attribute handling excerpt
 * 
 * 
 * @author Svanda
 *
 */
@Projection(name = "excerpt", types = SysSchemaAttributeHandling.class)
public interface SysSchemaAttributeHandlingExcerpt extends AbstractDtoProjection {
	
	String getIdmPropertyName();
	
	SysSchemaAttribute getSchemaAttribute();

	boolean isExtendedAttribute();

}
