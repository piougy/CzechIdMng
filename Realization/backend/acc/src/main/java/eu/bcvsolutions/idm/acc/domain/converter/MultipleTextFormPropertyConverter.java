package eu.bcvsolutions.idm.acc.domain.converter;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;

/**
 * String array connector property converter
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class MultipleTextFormPropertyConverter extends AbstractFormPropertyConverter {

	@Override
	public Set<String> getConnectorPropertyTypes() {
		return Sets.newHashSet("[Ljava.lang.String;","java.lang.String[]");
	}

	@Override
	public PersistentType getFormPropertyType() {
		return PersistentType.TEXT;
	}

	@Override
	public boolean isMultiple() {
		return true;
	}
}
