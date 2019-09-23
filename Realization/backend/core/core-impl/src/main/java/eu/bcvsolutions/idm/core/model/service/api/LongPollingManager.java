package eu.bcvsolutions.idm.core.model.service.api;

import org.joda.time.DateTime;
import org.springframework.web.context.request.async.DeferredResult;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.ModifiedFromFilter;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.rest.DeferredResultWrapper;
import eu.bcvsolutions.idm.core.rest.LongPollingSubscriber;

/**
 * Manager for long polling request
 * 
 * @since 9.7.6
 *
 * @author Vít Švanda
 *
 */
public interface LongPollingManager {
	
	public final String LONG_POLLING_ENABLED_KEY = "idm.pub.app.long-polling.enabled";

	/**
	 * Add new deferred result for type and ID defined inner result.
	 * 
	 * @param result
	 */
	void addSuspendedResult(DeferredResultWrapper result);

	/**
	 * Check deferred requests for given type
	 * 
	 * @param type
	 */
	void checkDeferredRequests(Class<? extends AbstractDto> type);

	/**
	 * If return false, then will be long-polling requests disabled. As default returns true.
	 * 
	 * @return
	 */
	boolean isLongPollingEnabled();

	/**
	 * Get last time stamp, when was given DTO modified.
	 * Works with modified and created field.
	 * 
	 * @param dto
	 * @return
	 */
	DateTime getLastTimeStamp(AbstractDto dto);

	/**
	 * Base implementation for check changes for given deferred result. 
	 * 
	 * @param deferredResult
	 * @param subscriber
	 * @param filter - Filter must be configured for get data only for given subscriber!
	 * @param service - Read service for subscriber type
	 * @param checkCount - If true, then count of entities will be check against last count in subscriber
	 */
	void baseCheckDeferredResult(DeferredResult<OperationResultDto> deferredResult, LongPollingSubscriber subscriber,
			ModifiedFromFilter filter, @SuppressWarnings("rawtypes") ReadDtoService service, boolean checkCount);

	/**
	 * Removes all subscribers that were last use before given time stamp. If
	 * clearBeforIt is not defined, the one hour is sets as default.
	 * 
	 * This method is call automatically every two hours (with null time stamp).
	 * 
	 * @param clearBeforIt
	 */
	void clearUnUseSubscribers(DateTime clearBeforIt);
	

}
