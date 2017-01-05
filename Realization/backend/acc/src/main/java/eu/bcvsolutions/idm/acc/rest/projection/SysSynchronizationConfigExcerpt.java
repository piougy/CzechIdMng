package eu.bcvsolutions.idm.acc.rest.projection;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.acc.entity.SysSynchronizationConfig;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;

/**
 * Synchronization config excerpt
 * 
 * 
 * @author Svanda
 *
 */
@Projection(name = "excerpt", types = SysSynchronizationConfig.class)
public interface SysSynchronizationConfigExcerpt extends AbstractDtoProjection {

	String getName();

	LocalDateTime getTimestamp();

	public boolean isEnabled();
	
	public boolean isReconciliation();
}
