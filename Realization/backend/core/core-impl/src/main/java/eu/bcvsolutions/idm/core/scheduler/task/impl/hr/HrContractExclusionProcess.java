package eu.bcvsolutions.idm.core.scheduler.task.impl.hr;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityContractFilter;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;

/**
 * HR process - identity's contract exclusion. The processes is started for
 * contracts that are both valid (meaning validFrom and validTill) and disabled.
 * 
 * @author Jan Helbich
 *
 */
@Service
@Description("HR process - contract exclusion")
@DisallowConcurrentExecution
public class HrContractExclusionProcess extends AbstractWorkflowStatefulExecutor<IdmIdentityContractDto> {

	private static final String PROCESS_NAME = "hrContractExclusion";

	@Autowired
	private IdmIdentityContractService identityContractService;

	/**
	 * {@inheritDoc}
	 * 
	 * Find all identity contracts, that are both valid and disabled.
	 */
	@Override
	public Page<IdmIdentityContractDto> getItemsToProcess(Pageable pageable) {
		IdentityContractFilter filter = new IdentityContractFilter();
		filter.setValid(Boolean.TRUE);
		filter.setDisabled(Boolean.TRUE);
		filter.setMain(Boolean.FALSE);
		return identityContractService.find(filter, pageable);
	}

	@Override
	public String getWorkflowName() {
		return PROCESS_NAME;
	}

}
