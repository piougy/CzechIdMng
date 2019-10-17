package eu.bcvsolutions.idm.core.scheduler.task.impl;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.config.ModelMapperChecker;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * tttt.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@DisallowConcurrentExecution
@Description("tttt.")
public class ModelMapperCheckerTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	@Autowired private ModelMapperChecker modelMapperChecker;
	
	@Override
	public Boolean process() {
		modelMapperChecker.verify();
		//
		return Boolean.TRUE;
	}
}
