package eu.bcvsolutions.idm.security.api.service;

import eu.bcvsolutions.idm.security.api.domain.Enabled;

/**
 * Evaluates {@link Enabled} annotation.
 * 
 * @author Radek Tomiška
 *
 */
public interface EnabledEvaluator {

	/**
	 * Returns true, if all of modules and configuration properties are enabled
	 * 
	 * @param ifEnabled
	 * @return
	 */
	boolean isEnabled(Enabled enabled);
	
	/**
	 * Returns true, if given bean is enabled (or does not have enabled annotation defined).
	 * 
	 * @param bean service, manager etc.
	 * @return
	 */
	boolean isEnabled(Object bean);
	
	/**
	 * Returns true, if given class is enabled (or does not have enabled annotation defined).
	 * 
	 * @param clazz service class, manager class etc.
	 * @return
	 */
	boolean isEnabled(Class<?> clazz);
	
}
