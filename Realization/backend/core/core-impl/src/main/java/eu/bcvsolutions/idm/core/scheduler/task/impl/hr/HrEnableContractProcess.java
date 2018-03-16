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
 * HR process - enable identity's contract process. The processes is started
 * for contracts that are both valid (meaning validFrom and validTill) and
 * enabled.
 * 
 * @author Jan Helbich
 * @since 7.5.1
 *
 */
@Service
@Description("HR process - enable active contract")
@DisallowConcurrentExecution
public class HrEnableContractProcess extends AbstractHrProcess {

	private static final String PROCESS_NAME = "hrEnableContract";

	@Autowired
	private IdmIdentityContractService identityContractService;

	public HrEnableContractProcess() {
	}
	
	public HrEnableContractProcess(boolean skipAutomaticRoleRecalculation) {
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
		filter.setValid(Boolean.TRUE);
		filter.setDisabled(Boolean.FALSE);
		return identityContractService.find(filter, pageable);
	}

	@Override
	public String getWorkflowName() {
		return PROCESS_NAME;
	}

}
