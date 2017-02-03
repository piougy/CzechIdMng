package eu.bcvsolutions.idm.acc.rest.projection;

import org.joda.time.LocalDateTime;
import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;

/**
 * Synchronization config excerpt
 * 
 * 
 * @author Svanda
 *
 */
@Projection(name = "excerpt", types = SysSyncConfig.class)
public interface SysSynchronizationConfigExcerpt extends AbstractDtoProjection {

	String getName();

	LocalDateTime getTimestamp();

	public boolean isEnabled();
	
	public boolean isReconciliation();
}
