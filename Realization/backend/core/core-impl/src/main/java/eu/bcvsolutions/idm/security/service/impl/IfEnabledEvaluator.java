package eu.bcvsolutions.idm.security.service.impl;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.security.api.domain.IfEnabled;
import eu.bcvsolutions.idm.security.exception.ConfigurationDisabledException;
import eu.bcvsolutions.idm.security.exception.ModuleDisabledException;

/**
 * Evaluates {@link IfEnabled} annotation.
 * 
 * @author Radek Tomiška
 *
 */
@Aspect
@Component
public class IfEnabledEvaluator {
	
	private final ModuleService moduleService;
	private final IdmConfigurationService configurationService;
	
	@Autowired
	public IfEnabledEvaluator(ModuleService moduleService, IdmConfigurationService configurationService) {
		Assert.notNull(moduleService, "ModuleService is required");
		Assert.notNull(configurationService, "IdmConfigurationService is configurationService");
		//
		this.moduleService = moduleService;
		this.configurationService = configurationService;
	}

	/**
	 * Checks enabled modules and configuration propertise
	 * 
	 * @param jp
	 * @param bean
	 * @param ifEnabled
	 * @throws ModuleDisabledException if any module is disabled
	 * @throws ConfigurationDisabledException if any property is disabled
	 */
	@Before(value = "target(bean) && (@annotation(ifEnabled) || @within(ifEnabled))", argNames="bean,ifEnabled")
	public void checkIsEnabled(JoinPoint jp, Object bean, IfEnabled ifEnabled) {
		// modules
		checkEnabledModules(ifEnabled.module());
		checkEnabledModules(ifEnabled.value());
		// properties
		checkEnabledProperties(ifEnabled.property());
	}
	
	/**
	 * Returns true, if all given modules are enabled, otherwise throws {@link ModuleDisabledException}.
	 * 
	 * @param modules
	 * @return
	 * @throws ModuleDisabledException
	 */
	private boolean checkEnabledModules(String[] modules) throws ModuleDisabledException {
		Assert.notNull(modules);	
		//
		for(String moduleId : modules) {
			if (!moduleService.isEnabled(moduleId)) {
				throw new ModuleDisabledException(moduleId);
			}
		}
		return true;
	}
	
	/**
	 * Returns true, if all given properties are enabled in {@link IdmConfigurationService}, otherwise throws {@link ConfigurationDisabledException}.
	 * 
	 * @param properties
	 * @return
	 * @throws ConfigurationDisabledException
	 */
	private boolean checkEnabledProperties(String[] properties) throws ConfigurationDisabledException {
		Assert.notNull(properties);
		//
		for(String property : properties) {
			if (!configurationService.getBooleanValue(property, false)) {
				throw new ConfigurationDisabledException(property);
			}
		}
		return true;
	}

}