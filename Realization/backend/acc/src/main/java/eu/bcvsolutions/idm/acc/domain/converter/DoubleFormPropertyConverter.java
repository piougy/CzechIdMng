package eu.bcvsolutions.idm.acc.domain.converter;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;

/**
 * Double connector property converter
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component
public class DoubleFormPropertyConverter extends AbstractFormPropertyConverter {

	@Override
	public Set<String> getConnectorPropertyTypes() {
		return Sets.newHashSet("java.math.BigDecimal", "java.lang.Double", "double");
	}

	@Override
	public PersistentType getFormPropertyType() {
		return PersistentType.DOUBLE;
	}
}
