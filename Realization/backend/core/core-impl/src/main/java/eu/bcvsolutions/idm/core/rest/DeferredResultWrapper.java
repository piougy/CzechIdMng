package eu.bcvsolutions.idm.core.rest;

import java.util.UUID;

import org.springframework.web.context.request.async.DeferredResult;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.model.service.api.CheckLongPollingResult;

/**
 * Deferred result wrapper. Contains deferred result, ID and type of entity,
 * callback for call implementation of check method.
 * 
 * @author Vít Švanda
 *
 */
public class DeferredResultWrapper {

		private UUID entityId;
		private DeferredResult<OperationResultDto> result;
		private Class<? extends AbstractDto> type;
		private CheckLongPollingResult checkResultCallback;
		
		public DeferredResultWrapper(UUID entityId, Class<? extends AbstractDto> type,
				DeferredResult<OperationResultDto> result) {
			this.result = result;
			this.entityId = entityId;
			this.type = type;
		}
	
		public DeferredResult<OperationResultDto> getResult() {
			return result;
		}

		public UUID getEntityId() {
			return entityId;
		}

		public Class<? extends AbstractDto> getType() {
			return type;
		}

		public CheckLongPollingResult getCheckResultCallback() {
			return checkResultCallback;
		}

		public void onCheckResultCallback(CheckLongPollingResult checkResultCallback) {
			this.checkResultCallback = checkResultCallback;
		}

		@Override
		public String toString() {
			return String.format("DeferredResultWrapper [entityId=%s, result=%s, type=%s, checkResultCallback=%s]",
					entityId, result, type, checkResultCallback);
		}
	}