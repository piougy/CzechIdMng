package eu.bcvsolutions.idm.core.api.bulk.action;


import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.Recoverable;


/**
 * Abstract redeploy bulk operation
 * for entities the service of which implements {@link Recoverable}.
 * 
 * @author Ondrej Husnik
 * @author Radek Tomiška
 * @since 10.6.0
 */
public abstract class AbstractRedeployBulkAction<DTO extends AbstractDto, F extends BaseFilter>
		extends AbstractBackupBulkAction<DTO, F> {

	@Override
	@SuppressWarnings("unchecked")
	protected OperationResult processDto(DTO dto) {
		try {
			Assert.notNull(dto, "Entity to redeploy is required!");
			Assert.notNull(dto.getId(), "Id of entity to redeploy is required!");
			Assert.isTrue(getService() instanceof Recoverable, "Entity service has to implement recoverable interface!");
			Recoverable<DTO> service = (Recoverable<DTO>) getService();
			// call redeploy
			service.redeploy(dto);
			//
			return new OperationResult(OperationState.EXECUTED);
		} catch (Exception ex) {
			return new OperationResult//
					.Builder(OperationState.EXCEPTION)//
							.setCause(ex)//
							.build();//
		}
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 1200;
	}
}
