package eu.bcvsolutions.idm.acc.domain.converter;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;

/**
 * Int connector property converter
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class IntFormPropertyConverter extends AbstractFormPropertyConverter {

	@Override
	public Set<String> getConnectorPropertyTypes() {
		return Sets.newHashSet("int");
	}

	@Override
	public PersistentType getFormPropertyType() {
		return PersistentType.INT;
	}

}
