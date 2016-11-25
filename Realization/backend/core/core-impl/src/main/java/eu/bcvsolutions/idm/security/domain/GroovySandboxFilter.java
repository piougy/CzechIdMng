package eu.bcvsolutions.idm.security.domain;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.kohsuke.groovy.sandbox.GroovyValueFilter;

import com.google.common.collect.ImmutableList;

import groovy.lang.Closure;
import groovy.lang.Script;

/**
 * This {@link org.kohsuke.groovy.sandbox.GroovyInterceptor} implements a
 * security check.
 *
 * @author Svanda
 */
public class GroovySandboxFilter extends GroovyValueFilter {
	private static final List<Class<?>> ALLOWED_TYPES = ImmutableList.of(String.class, Integer.class, Double.class,
			Long.class, Date.class, Enum.class, Boolean.class, BigDecimal.class, UUID.class, Character.class);

	List<Class<?>> allowedCustomTypes = new ArrayList<>();
	
	public GroovySandboxFilter() {

	}

	public GroovySandboxFilter(List<Class<?>> allowedTypes) {
		if(allowedTypes != null) {
			allowedCustomTypes.addAll(allowedTypes);
		}
	}

	@Override
	public Object filter(Object o) {
		if (o == null || ALLOWED_TYPES.contains(o.getClass()) || allowedCustomTypes.contains(o.getClass())) {
			return o;
		}
		if (o instanceof Class && ALLOWED_TYPES.contains(o) || allowedCustomTypes.contains(o)) {
			return o;
		}
		if (o instanceof Script || o instanceof Closure) {
			return o; // access to properties of compiled groovy script
		}
		String className = o.getClass().getName();
		if (o instanceof Class) {
			className = ((Class<?>) o).getName();
		}
		throw new SecurityException(MessageFormat.format("Script wants to use unauthorized class: [{0}] ", className));
	}

}
