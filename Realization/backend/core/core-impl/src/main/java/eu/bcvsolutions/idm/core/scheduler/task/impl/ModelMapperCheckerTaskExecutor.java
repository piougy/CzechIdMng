package eu.bcvsolutions.idm.core.scheduler.task.impl;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.config.ModelMapperChecker;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.exception.ConfigurationDisabledException;

/**
 * Check model mapper is properly initialized to prevent:
 * org.modelmapper.MappingException: ModelMapper mapping errors: Converter org.modelmapper.internal.converter.CollectionConverter@7214dbf8 failed to convert 
 * 
 * @author Radek Tomiška
 * @since 9.7.10
 */
@Component
@DisallowConcurrentExecution
@Description("Check model mapper is properly initialized to prevent dto mapping exceptions.")
public class ModelMapperCheckerTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	@Autowired private ModelMapperChecker modelMapperChecker;
	
	@Override
	public Boolean process() {
		if (!modelMapperChecker.verify()) {
			throw new ConfigurationDisabledException(ModelMapperChecker.PROPERTY_ENABLED);
		}
		//
		return Boolean.TRUE;
	}
}
