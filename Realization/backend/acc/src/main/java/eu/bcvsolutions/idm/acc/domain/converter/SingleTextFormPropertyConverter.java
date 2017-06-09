package eu.bcvsolutions.idm.acc.domain.converter;

import java.io.Serializable;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperty;

/**
 * String connector property converter. Supports guarded properties
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class SingleTextFormPropertyConverter extends AbstractFormPropertyConverter {
	
	private final FormService formService;
	
	@Autowired
	public SingleTextFormPropertyConverter(FormService formService) {
		Assert.notNull(formService);
		//
		this.formService = formService;
	}

	@Override
	public Set<String> getConnectorPropertyTypes() {
		return Sets.newHashSet("java.lang.String", "org.identityconnectors.common.security.GuardedString", GuardedString.class.getName());
	}

	@Override
	public PersistentType getFormPropertyType() {
		return PersistentType.TEXT;
	}
	
	@Override
	protected Object convertSingleConnectorPropertyValue(IcConfigurationProperty propertyConfiguration, AbstractFormValue<SysSystem> formValue) {
		if (formValue == null) {
			return null;
		}
		if (!propertyConfiguration.isConfidential()) {
			return formValue.getValue();
		}
		Serializable value = formService.getConfidentialPersistentValue(formValue);
		if (value == null) {
			return null;
		}
		return new org.identityconnectors.common.security.GuardedString(value.toString().toCharArray());
	}

}
