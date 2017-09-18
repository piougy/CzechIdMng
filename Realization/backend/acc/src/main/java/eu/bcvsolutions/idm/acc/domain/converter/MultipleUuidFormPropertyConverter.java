package eu.bcvsolutions.idm.acc.domain.converter;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;

/**
 * UUID array connector property converter
 * 
 * @author Svanda
 *
 */
@Component
public class MultipleUuidFormPropertyConverter extends AbstractFormPropertyConverter {

	@Override
	public Set<String> getConnectorPropertyTypes() {
		return Sets.newHashSet("[Ljava.util.UUID;", "java.util.UUID[]");
	}

	@Override
	public PersistentType getFormPropertyType() {
		return PersistentType.UUID;
	}

	@Override
	public boolean isMultiple() {
		return true;
	}
}
