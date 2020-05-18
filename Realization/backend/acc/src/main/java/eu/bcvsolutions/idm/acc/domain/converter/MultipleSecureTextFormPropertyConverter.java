package eu.bcvsolutions.idm.acc.domain.converter;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;

/**
 * GuardedString array connector property converter.
 * 
 * @author Roman Kucera
 * @since 10.3.0
 */
@Component
public class MultipleSecureTextFormPropertyConverter extends AbstractFormPropertyConverter {

	@Override
	public Set<String> getConnectorPropertyTypes() {
		return Sets.newHashSet("[Lorg.identityconnectors.common.security.GuardedString;", "org.identityconnectors.common.security.GuardedString[]");
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
