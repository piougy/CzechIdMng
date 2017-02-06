package eu.bcvsolutions.idm.core.scheduler.rest.projection;

import java.io.Serializable;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;

/**
 * Excerpt projection for {@link IdmLongRunningTask}.
 * 
 * @author Radek Tomiška
 *
 */
@Projection(name = "excerpt", types = IdmLongRunningTask.class)
public interface IdmLongRunningTaskExcerpt extends AbstractDtoProjection {
	
	String getTaskType();

	Long getCount();

	Long getCounter();
	
	OperationState getResultState();

	OperationResult getResult();
	
	String getTaskDescription();
	
	boolean isRunning();
	
	long getThreadId();
	
	String getInstanceId();
	
	String getThreadName();
	
	Serializable getTaskProperties();
}
