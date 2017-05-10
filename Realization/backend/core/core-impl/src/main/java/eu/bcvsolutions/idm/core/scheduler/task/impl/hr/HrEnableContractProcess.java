package eu.bcvsolutions.idm.core.scheduler.task.impl.hr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityContractFilter;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;

/**
 * HR process - enable identity's contract process. The processes is started
 * for contracts that are both valid (meaning validFrom and validTill) and
 * enabled.
 * 
 * @author Jan Helbich
 *
 */
public class HrEnableContractProcess extends AbstractWorkflowStatefulExecutor<IdmIdentityContractDto> {

	private static final String PROCESS_NAME = "hrEnableContract";

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
		filter.setValid(Boolean.TRUE);
		filter.setDisabled(Boolean.FALSE);
		filter.setMain(Boolean.FALSE);
		return identityContractService.find(filter, pageable);
	}

	@Override
	public String getWorkflowName() {
		return PROCESS_NAME;
	}

}
