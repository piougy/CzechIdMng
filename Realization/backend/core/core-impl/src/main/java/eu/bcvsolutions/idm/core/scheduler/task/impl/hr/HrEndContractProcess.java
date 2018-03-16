package eu.bcvsolutions.idm.core.scheduler.task.impl.hr;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;

/**
 * HR process - end of identity's contract process. The processes is started
 * for contracts that are not valid (meaning validFrom and validTill).
 * 
 * @author Jan Helbich
 * @since 7.5.1
 */
@Service
@Description("HR process - end of contract")
@DisallowConcurrentExecution
public class HrEndContractProcess extends AbstractHrProcess {

	private static final String PROCESS_NAME = "hrEndContract";

	@Autowired
	private IdmIdentityContractService identityContractService;

	public HrEndContractProcess() {
	}
	
	public HrEndContractProcess(boolean skipAutomaticRoleRecalculation) {
		super(skipAutomaticRoleRecalculation);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Find all identity contracts, that are both valid and enabled.
	 */
	@Override
	public Page<IdmIdentityContractDto> getItemsToProcess(Pageable pageable) {
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setValid(Boolean.FALSE);
		return identityContractService.find(filter, pageable);
	}

	@Override
	public String getWorkflowName() {
		return PROCESS_NAME;
	}

}
