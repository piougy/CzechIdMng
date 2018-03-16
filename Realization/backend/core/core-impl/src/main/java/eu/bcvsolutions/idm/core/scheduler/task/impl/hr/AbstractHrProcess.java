package eu.bcvsolutions.idm.core.scheduler.task.impl.hr;

import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.scheduler.task.impl.AbstractWorkflowStatefulExecutor;

/**
 * Abstract class for all HR process
 * - HrContractExclusionProcess
 * - HrEnableContractProcess
 * - HrEndContractProcess
 * The class extends HR process with skip autoamtic role recalculation.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public abstract class AbstractHrProcess extends AbstractWorkflowStatefulExecutor<IdmIdentityContractDto> {

	protected boolean skipAutomaticRoleRecalculation = false;
	
	public AbstractHrProcess() {
	}
	
	public AbstractHrProcess(boolean skipAutomaticRoleRecalculation) {
		this.skipAutomaticRoleRecalculation = skipAutomaticRoleRecalculation;
	}
	
	@Override
	protected Map<String, Object> getVariables() {
		Map<String, Object> variables = super.getVariables();
		variables.put(IdmAutomaticRoleAttributeService.SKIP_RECALCULATION, this.skipAutomaticRoleRecalculation);
		return variables;
	}
}
