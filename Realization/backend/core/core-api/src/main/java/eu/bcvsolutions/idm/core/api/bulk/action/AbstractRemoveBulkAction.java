package eu.bcvsolutions.idm.core.api.bulk.action;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

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

	@Override
	protected OperationResult processDto(DTO dto) {
		try {
			this.getService().delete(dto);
			return new OperationResult.Builder(OperationState.EXECUTED).build();
		} catch(ResultCodeException ex) {
			return new OperationResult.Builder(OperationState.EXCEPTION).setException(ex).build();
		} catch(Exception ex) {
			return new OperationResult.Builder(OperationState.EXCEPTION).setCause(ex).build();
		}
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 10000;
	}
	
}
