package eu.bcvsolutions.idm.acc.domain.converter;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;

/**
 * Long connector property converter
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class LongFormPropertyConverter extends AbstractFormPropertyConverter {

	@Override
	public Set<String> getConnectorPropertyTypes() {
		return Sets.newHashSet("long");
	}

	@Override
	public PersistentType getFormPropertyType() {
		return PersistentType.LONG;
	}

}
