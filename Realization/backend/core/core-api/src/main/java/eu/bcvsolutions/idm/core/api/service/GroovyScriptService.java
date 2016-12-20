package eu.bcvsolutions.idm.core.api.service;

import java.util.Map;

/**
 * Service for evaluate and validate groovy scripts
 * @author svandav
 *
 */
public interface GroovyScriptService {

	/**
	 * Evaluates some script against the current Binding and returns the result.
	 * Script all secured with groovy sandbox. Only classes defined in GroovySandboxFilter and classes
	 * from given variables will allowed in script.
	 * @param script
	 * @return
	 */
	Object evaluate(String script, Map<String, Object> variables);

	/**
	 * Validation script on compilation errors
	 * @param script
	 * @return
	 */
	Object validateScript(String script);

}