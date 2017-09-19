package eu.bcvsolutions.idm.core.security.service.impl;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

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
@Aspect
@Component
public class EnabledAspect {

	private final EnabledEvaluator enabledEvaluator;
	
	@Autowired
	public EnabledAspect(EnabledEvaluator enabledEvaluator) {
		Assert.notNull(enabledEvaluator, "IdmConfigurationService is configurationService");
		//
		this.enabledEvaluator = enabledEvaluator;
	}
	
	/**
	 * Checks enabled modules and configuration properties on type
	 * 
	 * @param joinPoint
	 * @param bean
	 * @param ifEnabled
	 * @return 
	 * @throws Throwable 
	 * @throws ModuleDisabledException if any module is disabled
	 * @throws ConfigurationDisabledException if any property is disabled
	 */
	@Around(value = "target(bean) && @within(enabled)", argNames="bean,enabled")
	public Object checkBeanEnabled(ProceedingJoinPoint joinPoint, Object bean, Enabled enabled) throws Throwable {
		if (checkEnabled(joinPoint, bean, enabled)) {
			return joinPoint.proceed();
		}
		return null;
	}
	
	/**
	 * Checks enabled modules and configuration properties on method
	 * 
	 * @param joinPoint
	 * @param bean
	 * @param ifEnabled
	 * @return 
	 * @throws ModuleDisabledException if any module is disabled
	 * @throws ConfigurationDisabledException if any property is disabled
	 */
	@Around(value = "target(bean) && @annotation(enabled)", argNames="bean,enabled")
	public Object checkMethodEnabled(ProceedingJoinPoint joinPoint, Object bean, Enabled enabled) throws Throwable {
		if (checkEnabled(joinPoint, bean, enabled)) {
			return joinPoint.proceed();
		}
		return null;
	}
	
	/**
	 * Checks enabled modules and configuration properties on method
	 * 
	 * @param joinPoint
	 * @param bean
	 * @param enabled
	 * @return Returns true, when method can be execuded. Return false, when method should be skipped silently, throw exception otherwise
	 * @throws ModuleDisabledException if any module is disabled
	 * @throws ConfigurationDisabledException if any property is disabled
	 */
	private boolean checkEnabled(JoinPoint joinPoint, Object bean, Enabled enabled) {
		if (isOrderMethod(joinPoint)) {
			// we are ignoring order method from check
			return true;
		}
		try {
			enabledEvaluator.checkEnabled(enabled);
			return true;
		} catch(ModuleDisabledException|ConfigurationDisabledException ex) {
			if (isEventMethod(joinPoint)) {
				// we are ignoring event method from throw exception - skip insted
				return false;
			}
			throw ex;
		}		
		
	}
	
	/**
	 * Order method is executable always, even when module is disabled (we need ordered components etc.)
	 * 
	 * @param joinPoint
	 * @return
	 */
	private boolean isOrderMethod(JoinPoint joinPoint) {
		Signature signature = joinPoint.getSignature();
		if (signature instanceof MethodSignature) {
			MethodSignature methodSignature = (MethodSignature) signature;
			if (methodSignature.getMethod().getParameters().length == 0 && methodSignature.getMethod().getName().equals("getOrder")) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Event method is skipped only, when module is disabled
	 * 
	 * @param joinPoint
	 * @return
	 */
	private boolean isEventMethod(JoinPoint joinPoint) {
		Signature signature = joinPoint.getSignature();
		if (signature instanceof MethodSignature) {
			MethodSignature methodSignature = (MethodSignature) signature;
			if (methodSignature.getMethod().getParameters().length == 1 
					&& ApplicationEvent.class.isAssignableFrom(methodSignature.getMethod().getParameters()[0].getType())) {
				return true;
			}
		}
		return false;
	}
	
}
