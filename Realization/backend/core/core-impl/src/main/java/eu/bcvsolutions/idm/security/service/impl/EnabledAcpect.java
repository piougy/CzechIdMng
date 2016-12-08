package eu.bcvsolutions.idm.security.service.impl;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.security.api.domain.Enabled;
import eu.bcvsolutions.idm.security.api.exception.ConfigurationDisabledException;
import eu.bcvsolutions.idm.security.api.exception.ModuleDisabledException;
import eu.bcvsolutions.idm.security.api.service.EnabledEvaluator;

/**
 * Evaluates {@link Enabled} annotation.
 * 
 * @author Radek Tomiška
 *
 */
@Aspect
@Component
public class EnabledAcpect {

	private final EnabledEvaluator enabledEvaluator;
	
	@Autowired
	public EnabledAcpect(EnabledEvaluator enabledEvaluator) {
		Assert.notNull(enabledEvaluator, "IdmConfigurationService is configurationService");
		//
		this.enabledEvaluator = enabledEvaluator;
	}
	
	/**
	 * Checks enabled modules and configuration properties
	 * 
	 * @param joinPoint
	 * @param bean
	 * @param ifEnabled
	 * @throws ModuleDisabledException if any module is disabled
	 * @throws ConfigurationDisabledException if any property is disabled
	 */
	@Before(value = "target(bean) && (@annotation(ifEnabled) || @within(ifEnabled))", argNames="bean,ifEnabled")
	public void checkIsEnabled(JoinPoint joinPoint, Object bean, Enabled ifEnabled) {
		Signature signature = joinPoint.getSignature();
		if (signature instanceof MethodSignature) {
			MethodSignature methodSignature = (MethodSignature) signature;
			// order method is needed, even when module is disabled (we need modules ordered)
			if (methodSignature.getMethod().getParameters().length == 0 && methodSignature.getMethod().getName().equals("getOrder")) {
				return;
			}
		}
		//
		enabledEvaluator.checkEnabled(ifEnabled);
	}
	
}
