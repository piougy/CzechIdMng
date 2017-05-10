package eu.bcvsolutions.idm.core.scheduler.task.impl.hr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityContractFilter;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;

/**
 * HR process - end of identity's contract process. The processes is started
 * for contracts that are not valid (meaning validFrom and validTill).
 * 
 * @author Jan Helbich
 *
 */
public class HrEndContractProcess extends AbstractWorkflowStatefulExecutor<IdmIdentityContractDto> {

	private static final String PROCESS_NAME = "hrEndContract";

	@Autowired
	private IdmIdentityContractService identityContractService;

	/**
	 * {@inheritDoc}
	 * 
	 * Find all identity contracts, that are both valid and enabled.
	 */
	@Override
	public Page<IdmIdentityContractDto> getItemsToProcess(Pageable pageable) {
		IdentityContractFilter filter = new IdentityContractFilter();
		filter.setValid(Boolean.FALSE);
		filter.setMain(Boolean.FALSE);
		return identityContractService.find(filter, pageable);
	}

	@Override
	public String getWorkflowName() {
		return PROCESS_NAME;
	}

}
