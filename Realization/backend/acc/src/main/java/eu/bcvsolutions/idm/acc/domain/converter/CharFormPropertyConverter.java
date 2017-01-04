package eu.bcvsolutions.idm.acc.domain.converter;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.eav.api.domain.PersistentType;

/**
 * Char connector property converter
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class CharFormPropertyConverter extends AbstractFormPropertyConverter {

	@Override
	public Set<String> getConnectorPropertyTypes() {
		return Sets.newHashSet("char");
	}

	@Override
	public PersistentType getFormPropertyType() {
		return PersistentType.CHAR;
	}

}
