package eu.bcvsolutions.idm.acc.domain.converter;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperty;

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
	public IdmFormAttributeDto toFormAttribute(IcConfigurationProperty propertyConfiguration) {
		IdmFormAttributeDto attribute =  super.toFormAttribute(propertyConfiguration);
		// we need to set select face for required properties => default face requires set property to TRUE, connector framework needs to fill some value
		if (propertyConfiguration.isRequired()) {
			attribute.setFaceType(BaseFaceType.BOOLEAN_SELECT);
		}
		return attribute;
	}

	@Override
	public PersistentType getFormPropertyType() {
		return PersistentType.BOOLEAN;
	}

}
