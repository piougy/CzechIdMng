package eu.bcvsolutions.idm.acc.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;

/**
 * Synchronization item log excerpt
 * 
 * 
 * @author Svanda
 *
 */
@Projection(name = "excerpt", types = SysSyncItemLog.class)
public interface SysSyncItemLogExcerpt extends AbstractDtoProjection {

	public SysSyncActionLog getSyncActionLog();

	public String getIdentification();

	public String getDisplayName();

	public String getMessage();

	public String getLog();

	public String getType();

}
