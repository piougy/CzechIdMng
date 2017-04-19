package eu.bcvsolutions.idm.core.security.service.impl;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.exception.ConfigurationDisabledException;
import eu.bcvsolutions.idm.core.security.api.exception.ModuleDisabledException;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

/**
 * Evaluates {@link Enabled} annotation.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultEnabledEvaluator implements EnabledEvaluator {
	
	private final ModuleService moduleService;
	private final ConfigurationService configurationService;
	
	public DefaultEnabledEvaluator(ModuleService moduleService, ConfigurationService configurationService) {
		Assert.notNull(moduleService, "ModuleService is required");
		Assert.notNull(configurationService, "ConfigurationService is configurationService");
		//
		this.moduleService = moduleService;
		this.configurationService = configurationService;
	}
	
	/**
	 * Returns true, if all of modules and configuration properties are enabled
	 * 
	 * @param ifEnabled
	 * @return
	 */
	@Override
	public boolean isEnabled(Enabled enabled) {
		Assert.notNull(enabled);
		//
		try {
			checkEnabled(enabled);
		} catch(ModuleDisabledException|ConfigurationDisabledException ex) {
			return false;
		}
		return true;
	}
	
	/**
	 * Returns true, if given bean is enabled (or does not have enabled annotation defined).
	 * 
	 * @param bean service, manager etc.
	 * @return
	 */
	@Override
	public boolean isEnabled(Object bean) {
		Assert.notNull(bean);
		//		
		return isEnabled(AopProxyUtils.ultimateTargetClass(bean));
	}
	
	/**
	 * Returns true, if given class is enabled (or does not have enabled annotation defined).
	 * 
	 * @param bean service, manager etc.
	 * @return
	 */
	@Override
	public boolean isEnabled(Class<?> clazz) {
		Assert.notNull(clazz);
		//
		Enabled enabled = clazz.getAnnotation(Enabled.class);
		if (enabled == null) {
			// bean is enabled
			return true;
		}
		return isEnabled(enabled);
	}
	
	/**
	 * Checks enabled modules and configuration properties
	 * 
	 * @param ifEnabled
	 */
	@Override
	public void checkEnabled(Enabled enabled) {
		Assert.notNull(enabled, "Enabled annotation is required for evaluating");
		// modules
		checkEnabledModules(enabled.module());
		checkEnabledModules(enabled.value());
		// properties
		checkEnabledProperties(enabled.property());
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