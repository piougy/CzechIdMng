package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.kohsuke.groovy.sandbox.GroovyInterceptor;
import org.kohsuke.groovy.sandbox.SandboxTransformer;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.security.domain.GroovySandboxFilter;
import eu.bcvsolutions.idm.core.security.exception.IdmSecurityException;
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
		GroovySandboxFilter sandboxFilter = null;
		Collection<?> dis = null;
		//
		try {
			// if groovy filter exist add extraAllowedClasses, into this filter, otherwise create new
			if (!GroovyInterceptor.getApplicableInterceptors().isEmpty()) {
				// exists only one goovy filter
				sandboxFilter = (GroovySandboxFilter) GroovyInterceptor.getApplicableInterceptors().get(0);
				dis = CollectionUtils.disjunction(sandboxFilter.getCustomTypes(), allowedVariableClass);
				sandboxFilter.addCustomTypes(allowedVariableClass);
			} else {
				sandboxFilter = new GroovySandboxFilter(allowedVariableClass);
				sandboxFilter.register();
			}

			return shell.evaluate(script);
		} catch (SecurityException ex) {
			throw new IdmSecurityException(CoreResultCode.GROOVY_SCRIPT_SECURITY_VALIDATION, ImmutableMap.of("message", ex.getLocalizedMessage()), ex);
		} finally {
			// if this script is called from another script, remove only allowed classes from them
			// otherwise unregister all filter.
			if (dis == null) {
				sandboxFilter.unregister();
			} else {
				sandboxFilter.clearCustomTypes(dis);
			}
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
			throw new ResultCodeException(CoreResultCode.GROOVY_SCRIPT_VALIDATION, ex);
		}
	}

}
