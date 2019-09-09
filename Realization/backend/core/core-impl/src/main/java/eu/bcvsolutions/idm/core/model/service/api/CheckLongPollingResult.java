package eu.bcvsolutions.idm.core.model.service.api;

import org.springframework.web.context.request.async.DeferredResult;

import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.rest.LongPollingSubscriber;

/**
 * Callback interface for check deferred result.
 * 
 * @author Vít Švanda
 *
 */
public interface CheckLongPollingResult {
	
	public void checkDeferredResult(DeferredResult<OperationResultDto> result, LongPollingSubscriber subscriber);

}
