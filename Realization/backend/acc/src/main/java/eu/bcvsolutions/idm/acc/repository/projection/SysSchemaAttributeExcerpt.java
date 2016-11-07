package eu.bcvsolutions.idm.acc.repository.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.core.api.repository.projection.AbstractDtoProjection;

/**
 * Schema attribute excerpt
 * 
 * 
 * @author Svanda
 *
 */
@Projection(name = "excerpt", types = SysSchemaAttribute.class)
public interface SysSchemaAttributeExcerpt extends AbstractDtoProjection {

}
