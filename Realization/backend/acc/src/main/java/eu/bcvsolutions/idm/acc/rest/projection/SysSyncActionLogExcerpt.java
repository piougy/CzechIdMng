package eu.bcvsolutions.idm.acc.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;

/**
 * Synchronization action log excerpt
 * 
 * 
 * @author Svanda
 *
 */
@Projection(name = "excerpt", types = SysSyncActionLog.class)
public interface SysSyncActionLogExcerpt extends AbstractDtoProjection {
	
	public SynchronizationActionType getSyncAction();

	public Integer getOperationCount();
	
	public OperationResultType getOperationResult();



}
