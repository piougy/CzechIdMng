package eu.bcvsolutions.idm.acc.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;

/**
 * Schema object class excerpt
 * 
 * 
 * @author Svanda
 *
 */
@Projection(name = "excerpt", types = SysSchemaObjectClass.class)
public interface SysSchemaObjectClassExcerpt extends AbstractDtoProjection {

	boolean isAuxiliary();
	
	boolean isContainer();

	String getObjectClassName();
}
