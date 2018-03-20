package eu.bcvsolutions.idm.core.scheduler.task.impl.hr;

import java.util.List;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.scheduler.task.impl.AbstractWorkflowStatefulExecutor;

/**
 * Abstract class for all HR process
 * - HrContractExclusionProcess
 * - HrEnableContractProcess
 * - HrEndContractProcess
 * The class extends HR process with skip automatic role recalculation.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
public abstract class AbstractHrProcess extends AbstractWorkflowStatefulExecutor<IdmIdentityContractDto> {

	private static final String PARAMETER_WF = "Workflow definition";
	private boolean skipAutomaticRoleRecalculation = false;
	private String workflowName;
	
	public AbstractHrProcess() {
	}
	
	public AbstractHrProcess(boolean skipAutomaticRoleRecalculation) {
		this.skipAutomaticRoleRecalculation = skipAutomaticRoleRecalculation;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		workflowName = getParameterConverter().toString(properties, PARAMETER_WF);
	}
	
	@Override
	protected Map<String, Object> getVariables() {
		Map<String, Object> variables = super.getVariables();
		variables.put(IdmAutomaticRoleAttributeService.SKIP_RECALCULATION, this.skipAutomaticRoleRecalculation);
		return variables;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_WF);
		return parameters;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = super.getProperties();
		properties.put(PARAMETER_WF, workflowName);
		return properties;
	}
	
	public boolean isSkipAutomaticRoleRecalculation() {
		return skipAutomaticRoleRecalculation;
	}
	
	@Override
	public String getWorkflowName() {
		return workflowName;
	}
}
