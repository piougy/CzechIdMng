package eu.bcvsolutions.idm.core.security.domain;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.kohsuke.groovy.sandbox.GroovyValueFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import groovy.lang.Closure;
import groovy.lang.Script;

/**
 * This {@link org.kohsuke.groovy.sandbox.GroovyInterceptor} implements a
 * security check.
 *
 * @author Svanda
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class GroovySandboxFilter extends GroovyValueFilter {
	
	private static final Set<Class<?>> ALLOWED_TYPES = Sets.newHashSet(
			String.class, Integer.class, Double.class, Long.class, Date.class, Enum.class, Boolean.class, 
			BigDecimal.class, UUID.class, Character.class, GuardedString.class, DateTimeFormatter.class,
			DateTimeFormat.class, DateTime.class, String[].class, LocalDateTime.class,
			Map.class, HashMap.class, List.class, ArrayList.class,
			LoggerFactory.class, Logger.class, ch.qos.logback.classic.Logger.class);

	private final LinkedList<List<Class<?>>> allowedCustomTypes = new LinkedList<>();
	
	public GroovySandboxFilter() {

	}

	public GroovySandboxFilter(List<Class<?>> allowedTypes) {
		if(allowedTypes != null) {
			allowedCustomTypes.push(allowedTypes);
		}
	}
	
	public void addCustomTypes(List<Class<?>> allowedTypes) {
		if(allowedTypes != null) {
			allowedCustomTypes.push(allowedTypes);
		}
	}

	
	public Collection<Class<?>> getCustomTypes() {
		return allowedCustomTypes.peek();
	}
	
	public Collection<Class<?>> removeLastCustomTypes() {
		return allowedCustomTypes.pop();
	}

	public boolean isCustomTypesLast() {
		return this.allowedCustomTypes.size() == 1;
	}
	
	@Override
	public Object filter(Object o) {
		if (o == null) {
			return o;
		}
		Class<?> targetClass = AopUtils.getTargetClass(o);
		if (ALLOWED_TYPES.contains(targetClass) || getCustomTypes().contains(targetClass)) {
			return o;
		}
		if (o instanceof Class && ALLOWED_TYPES.contains(o) || getCustomTypes().contains(o)) {
			return o;
		}
		if (o instanceof Script || o instanceof Closure) {
			return o; // access to properties of compiled groovy script
		}
		String className = targetClass.getCanonicalName();
		throw new SecurityException(MessageFormat.format("Script wants to use unauthorized class: [{0}] ", className));
	}

}
