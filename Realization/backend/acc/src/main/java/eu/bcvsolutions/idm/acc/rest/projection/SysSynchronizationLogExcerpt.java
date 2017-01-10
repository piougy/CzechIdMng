package eu.bcvsolutions.idm.acc.rest.projection;

import org.joda.time.LocalDateTime;
import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.entity.SysSynchronizationLog;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;

/**
 * Synchronization log excerpt
 * 
 * 
 * @author Svanda
 *
 */
@Projection(name = "excerpt", types = SysSynchronizationLog.class)
public interface SysSynchronizationLogExcerpt extends AbstractDtoProjection {

	public boolean isRunning();

	public LocalDateTime getStarted();

	public LocalDateTime getEnded();

	public String getToken();
	
	public int getExceptionCreateEntity();
	
	public int getSuccessCreateEntity();
	
	public int getExceptionUpdateEntity();
	
	public int getSuccessUpdateEntity();
	
	public int getSuccessDeleteEntity();
	
	public int getExceptionDeleteEntity();
	
	public int getExceptionCreateAccount();

	public int getSuccessCreateAccount();
	
	public int getExceptionUpdateAccount();

	public int getSuccessUpdateAccount();

	public int getExceptionDeleteAccount();

	public int getSuccessDeleteAccount();

}
