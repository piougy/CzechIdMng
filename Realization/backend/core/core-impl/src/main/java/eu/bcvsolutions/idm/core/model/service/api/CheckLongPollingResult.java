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
	
	/**
	 * Check of the given deferred result on existing some changes.
	 * 
	 * @param result
	 * @param subscriber - Subscriber (keeps more metadata) for same entity ID
	 */
	void checkDeferredResult(DeferredResult<OperationResultDto> result, LongPollingSubscriber subscriber);

}
