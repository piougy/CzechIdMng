package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.Map;

/**
 * Service for evaluate and validate groovy scripts
 *
 * @author svandav
 */
public interface GroovyScriptService {

	/**
	 * Evaluates script and returns the result.
	 * Script is secured with groovy sandbox. Only classes defined in GroovySandboxFilter and classes
	 * from given variables will allowed in script.
     *
	 * @param script
	 * @param variables
	 * @return
	 */
	Object evaluate(String script, Map<String, Object> variables);

	/**
	 * Validation script on compilation errors
     *
	 * @param script
	 * @return
	 */
	Object validateScript(String script);

	/**
	 * Evaluates script and returns the result.
	 * Script is secured with groovy sandbox. Only classes defined in GroovySandboxFilter, classes
	 * from given variables will allowed in script and classes from extraAllowedCalsses parameter.
     *
	 * @param script
	 * @param variables
	 * @param extraAllowedClasses
	 * @return
	 */
	Object evaluate(String script, Map<String, Object> variables, List<Class<?>> extraAllowedClasses);

}