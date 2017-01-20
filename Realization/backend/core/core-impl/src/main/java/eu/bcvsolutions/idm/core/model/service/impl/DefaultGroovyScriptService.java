package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.kohsuke.groovy.sandbox.SandboxTransformer;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.security.domain.GroovySandboxFilter;
import eu.bcvsolutions.idm.security.exception.IdmSecurityException;
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
		return evaluate(script, variables, null);
	}
	
	@Override
	public Object evaluate(String script, Map<String, Object> variables, List<Class<?>> extraAllowedClasses) {
		Assert.notNull(script);

		Binding binding = new Binding(variables);
		GroovyShell shell = new GroovyShell(binding,
				new CompilerConfiguration().addCompilationCustomizers(new SandboxTransformer()));
		List<Class<?>> allowedVariableClass = resolveCustomAllowTypes(variables);
		if(extraAllowedClasses != null){
			allowedVariableClass.addAll(extraAllowedClasses);
		}

		GroovySandboxFilter sandboxFilter = new GroovySandboxFilter(allowedVariableClass);
		try {
			sandboxFilter.register();

			return shell.evaluate(script);
		} catch (SecurityException ex) {
			throw new IdmSecurityException(CoreResultCode.GROVY_SCRIPT_SECURITY_VALIDATION, ImmutableMap.of("message", ex.getLocalizedMessage()), ex);
		} finally {
			sandboxFilter.unregister();
		}
	}

	/**
	 * Return all unique class from variables. If is variable list, then add all
	 * classes for all items.
	 * 
	 * @param variables
	 * @return
	 */
	private List<Class<?>> resolveCustomAllowTypes(Map<String, Object> variables) {
		List<Class<?>> allowType = new ArrayList<>();
		if (variables == null) {
			return allowType;
		}
		variables.forEach((key, object) -> {
			if (object != null && !allowType.contains(object.getClass())) {
				allowType.add(object.getClass());
			}
			// We have to add types for all list items
			if (object instanceof List) {
				((List<?>) object).stream().forEach(item -> {
					if (item != null && !allowType.contains(item.getClass())) {
						allowType.add(item.getClass());
					}
				});
			}
		});
		return allowType;
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
