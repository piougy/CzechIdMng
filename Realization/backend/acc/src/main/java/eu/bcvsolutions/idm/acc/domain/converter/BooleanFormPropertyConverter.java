package eu.bcvsolutions.idm.acc.domain.converter;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.eav.api.domain.PersistentType;

/**
 * Boolean connector property converter
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class BooleanFormPropertyConverter extends AbstractFormPropertyConverter {

	@Override
	public Set<String> getConnectorPropertyTypes() {
		return Sets.newHashSet("java.lang.Boolean", "boolean");
	}

	@Override
	public PersistentType getFormPropertyType() {
		return PersistentType.BOOLEAN;
	}

}
