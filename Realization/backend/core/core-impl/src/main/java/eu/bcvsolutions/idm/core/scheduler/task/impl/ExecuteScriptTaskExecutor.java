package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;
import java.util.Map;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.script.evaluator.DefaultSystemScriptEvaluator;

/**
 * Long running task for execute script by code.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@Component(ExecuteScriptTaskExecutor.TASK_NAME)
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Description("Long running task for execute script by code.")
public class ExecuteScriptTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExecuteScriptTaskExecutor.class);
	public static final String TASK_NAME = "core-execute-script-long-running-task";
	private static String PARAMETER_SCRIPT_CODE = "scriptCode";
	private String scriptCode;

	@Autowired
	private DefaultSystemScriptEvaluator scriptEvaluator;
	
	@Override
	public String getName() {
		return TASK_NAME;
	}

	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		this.scriptCode = getParameterConverter().toString(properties, PARAMETER_SCRIPT_CODE);
		//
		getScript(this.scriptCode);
	}

	@Override
	public Boolean process() {

		LOG.info("Start script with code: [{}]", scriptCode);
		
		scriptEvaluator.evaluate(
				scriptEvaluator.newBuilder()
					.setScriptCode(getScript(scriptCode).getCode())
					.addParameter("scriptEvaluator", scriptEvaluator)
					.addParameter("task", this)
					.build());

		return Boolean.TRUE;
	}

	private IdmScriptDto getScript(String identifier) {
		IdmScriptDto script = getLookupService().lookupDto(IdmScriptDto.class, identifier);
		if (script == null) {
			LOG.error("Script with code: [{}], not found.", identifier);
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", identifier));
		}
		return script;
	}

	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_SCRIPT_CODE);
		return parameters;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto scriptKey = new IdmFormAttributeDto(
				PARAMETER_SCRIPT_CODE,
				PARAMETER_SCRIPT_CODE, 
				PersistentType.UUID);
		scriptKey.setRequired(true);
		scriptKey.setFaceType(BaseFaceType.SCRIPT_SELECT);
		//
		return Lists.newArrayList(scriptKey);
	}
	
	@Override
    public boolean isRecoverable() {
    	return true;
    }
}
