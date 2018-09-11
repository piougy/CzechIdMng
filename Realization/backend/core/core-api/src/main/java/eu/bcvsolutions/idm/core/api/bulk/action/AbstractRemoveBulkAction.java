package eu.bcvsolutions.idm.core.api.bulk.action;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.config.domain.RequestConfiguration;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.RequestManager;
import eu.bcvsolutions.idm.core.api.utils.ExceptionUtils;

/**
 * Abstract class for all remove operations
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 * @param <DTO>
 * @param <F>
 */

public abstract class AbstractRemoveBulkAction<DTO extends AbstractDto, F extends BaseFilter>
		extends AbstractBulkAction<DTO, F> {

	@Autowired
	private RequestConfiguration requestConfiguration;
	@Autowired
	private RequestManager<Requestable> requestManager;

	@SuppressWarnings("unchecked")
	@Override
	protected OperationResult processDto(DTO dto) {
		try {
			if (dto instanceof Requestable
					&& requestConfiguration.isRequestModeEnabled((Class<Requestable>) dto.getClass())) {
				// Request mode is enabled for that DTO
				Requestable requestable = (Requestable) dto;
				requestManager.deleteRequestable(requestable, false);

				return new OperationResult.Builder(OperationState.EXECUTED).build();
			}
			this.getService().delete(dto);
			return new OperationResult.Builder(OperationState.EXECUTED).build();
		} catch (AcceptedException ex) {
			return new OperationResult.Builder(OperationState.RUNNING).setException(ex).build();
		} catch (ResultCodeException ex) {
			return new OperationResult.Builder(OperationState.EXCEPTION).setException(ex).build();
		} catch (Exception ex) {
			Throwable resolvedException = ExceptionUtils.resolveException(ex);
			if (resolvedException instanceof ResultCodeException) {
				return new OperationResult.Builder(OperationState.EXCEPTION) //
						.setException((ResultCodeException) resolvedException) //
						.build(); //
			}
			return new OperationResult.Builder(OperationState.EXCEPTION).setCause(ex).build();
		}
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 10000;
	}

}
