package eu.bcvsolutions.idm.core.api.bulk.action;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.DefaultErrorModel;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.Recoverable;


/**
 * Abstract redeploy bulk operation
 * for entities the service of which implements {@link Recoverable}
 * 
 * @author Ondrej Husnik
 *
 */
public abstract class AbstractRedeployBulkAction<DTO extends AbstractDto, F extends BaseFilter>
		extends AbstractBulkAction<DTO, F> {

	private OperationResult itemException;
	
	@Autowired
	private ConfigurationService configurationService;

	@SuppressWarnings("unchecked")
	@Override
	protected OperationResult processDto(DTO dto) {
		try {
			Assert.notNull(dto, "Entity to redeploy is required!");
			Assert.notNull(dto.getId(), "Id of entity to redeploy is required!");
			Assert.isTrue(getService() instanceof Recoverable, "Entity service has to implement recoverable interface!");
			Recoverable<DTO> service = (Recoverable<DTO>) getService();
			// call redeploy
			service.redeploy(dto);
		} catch (Exception ex) {
			itemException = new OperationResult//
					.Builder(OperationState.EXCEPTION)//
							.setCause(ex)//
							.build();//
			return itemException;
		}
		return new OperationResult(OperationState.EXECUTED);
	}
	
	@Override
	public ResultModels prevalidate() {
		ResultModels results = new ResultModels();
		String backupDir = configurationService.getValue(Recoverable.BACKUP_FOLDER_CONFIG);
		if(StringUtils.isEmpty(StringUtils.stripToEmpty(backupDir))) {
			ResultModel result = new DefaultErrorModel(CoreResultCode.BACKUP_FOLDER_NOT_FOUND, ImmutableMap.of("property", Recoverable.BACKUP_FOLDER_CONFIG));
			results.addInfo(result);
		}
		return results;
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 10000;
	}
}
