package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

/**
 * Service for evaluate groovy scripts
 * 
 * @author svandav
 *
 */
@Service
public class DefaultGroovyScriptService implements GroovyScriptService {

	@Override
	public Object evaluate(String script, Map<String, Object> variables) {
		Assert.notNull(script);

		Binding binding = new Binding(variables);
		GroovyShell shell = new GroovyShell(binding);
		return shell.evaluate(script);
	}

	@Override
	public Object validateScript(String script) throws ResultCodeException {
		Assert.notNull(script);
		try {
			GroovyShell shell = new GroovyShell();
			return shell.parse(script);
		} catch (CompilationFailedException ex) {
			throw new ResultCodeException(CoreResultCode.GROVY_SCRIPT_VALIDATION, ex);
		}
	}

}
