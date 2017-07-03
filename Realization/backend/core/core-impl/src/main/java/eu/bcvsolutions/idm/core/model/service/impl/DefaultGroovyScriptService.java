package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.kohsuke.groovy.sandbox.GroovyInterceptor;
import org.kohsuke.groovy.sandbox.SandboxTransformer;
import org.springframework.aop.support.AopUtils;
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

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultGroovyScriptService.class);
	
	@Override
	public Object evaluate(String script, Map<String, Object> variables) {
		return evaluate(script, variables, null);
	}
	
	@Override
	public Object evaluate(String script, Map<String, Object> variables, List<Class<?>> extraAllowedClasses) {
		Assert.notNull(script);

		Binding binding = new Binding(variables);
		CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
		compilerConfiguration.setVerbose(true);
		compilerConfiguration.addCompilationCustomizers(new SandboxTransformer());
		GroovyShell shell = new GroovyShell(binding, compilerConfiguration);
		List<Class<?>> allowedVariableClass = resolveCustomAllowTypes(variables);
		if(extraAllowedClasses != null){
			allowedVariableClass.addAll(extraAllowedClasses);
		}
		GroovySandboxFilter sandboxFilter = null;
		//
		try {
			// if groovy filter exist add extraAllowedClasses, into this filter, otherwise create new
			if (!GroovyInterceptor.getApplicableInterceptors().isEmpty()) {
				// exists only one goovy filter
				sandboxFilter = (GroovySandboxFilter) GroovyInterceptor.getApplicableInterceptors().get(0);
				sandboxFilter.addCustomTypes(allowedVariableClass);
			} else {
				sandboxFilter = new GroovySandboxFilter(allowedVariableClass);
				sandboxFilter.register();
			}

			return shell.evaluate(script);
		} catch (SecurityException | IdmSecurityException ex) {
			LOG.error("SecurityException [{}]", ex.getLocalizedMessage());
			throw new IdmSecurityException(CoreResultCode.GROOVY_SCRIPT_SECURITY_VALIDATION, ImmutableMap.of("message", ex.getLocalizedMessage()), ex);
		} catch (Exception e) {
			LOG.error("Exception [{}]", e.getLocalizedMessage());
			throw new ResultCodeException(CoreResultCode.GROOVY_SCRIPT_EXCEPTION, ImmutableMap.of("message", e.getLocalizedMessage()), e);
		} finally {
			// if this script is called from another script, remove only allowed classes from them
			// otherwise unregister all filter.
			if (sandboxFilter.isCustomTypesLast()) {
				sandboxFilter.unregister();
			} else {
				sandboxFilter.removeLastCustomTypes();
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
			Class<?> targetClass = null;
			if (object != null) {
				targetClass = AopUtils.getTargetClass(object);
			}
			if (targetClass != null && !allowType.contains(targetClass)) {
				allowType.add(targetClass);
			}
			// We have to add types for all list items
			if (object instanceof List) {
				((List<?>) object).stream().forEach(item -> {
					if (item != null && !allowType.contains(AopUtils.getTargetClass(item))) {
						allowType.add(AopUtils.getTargetClass(item));
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
