package eu.bcvsolutions.idm.acc.rest.projection;

import java.util.List;

import org.joda.time.LocalDateTime;
import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;

/**
 * Synchronization log excerpt
 * 
 * 
 * @author Svanda
 *
 */
@Projection(name = "excerpt", types = SysSyncLog.class)
public interface SysSynchronizationLogExcerpt extends AbstractDtoProjection {

	public boolean isRunning();

	public LocalDateTime getStarted();

	public LocalDateTime getEnded();
	
	public List<SysSyncActionLog> getSyncActionLogs();

	public String getToken();

	public boolean isContainsError();

}
