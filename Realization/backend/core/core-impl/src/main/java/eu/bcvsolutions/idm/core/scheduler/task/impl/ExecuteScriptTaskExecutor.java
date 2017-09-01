package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;
import java.util.Map;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.service.api.IdmScriptService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.script.evaluator.DefaultSystemScriptEvaluator;

/**
 * Long running task for execute script by code.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Description("Long running task for execute script by code.")
public class ExecuteScriptTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExecuteScriptTaskExecutor.class);
	private static String PARAMETER_SCRIPT_CODE = "scriptCode";
	private String scriptCode;

	@Autowired
	private IdmScriptService scriptService;

	@Autowired
	private DefaultSystemScriptEvaluator scriptEvaluator;

	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		this.scriptCode = getParameterConverter().toString(properties, PARAMETER_SCRIPT_CODE);
		//
		getScriptByCode(this.scriptCode);
	}

	@Override
	public Boolean process() {

		LOG.info("Start script with code: [{}]", scriptCode);
		
		scriptEvaluator.evaluate(
				scriptEvaluator.newBuilder()
					.setScriptCode(scriptCode)
					.addParameter("scriptEvaluator", scriptEvaluator)
					.addParameter("task", this)
					.build());

		return Boolean.TRUE;
	}

	private IdmScriptDto getScriptByCode(String code) {
		IdmScriptDto script = scriptService.getByCode(code);
		if (script == null) {
			LOG.error("Script with code: [{}], not found.", code);
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", code));
		}
		return script;
	}

	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_SCRIPT_CODE);
		return parameters;
	}
}
