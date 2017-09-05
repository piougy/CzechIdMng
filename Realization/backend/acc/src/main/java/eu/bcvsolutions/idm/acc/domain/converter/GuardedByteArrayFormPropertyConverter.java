package eu.bcvsolutions.idm.acc.domain.converter;

import java.io.Serializable;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperty;

/**
 * Guarded Byte array connector property converter
 *  
 * @author Radek Tomiška
 *
 */
@Component
public class GuardedByteArrayFormPropertyConverter extends AbstractFormPropertyConverter {

	private final FormService formService;
	
	@Autowired
	public GuardedByteArrayFormPropertyConverter(FormService formService) {
		Assert.notNull(formService);
		//
		this.formService = formService;
	}
	
	@Override
	public Set<String> getConnectorPropertyTypes() {
		return Sets.newHashSet("org.identityconnectors.common.security.GuardedByteArray");
	}

	@Override
	public PersistentType getFormPropertyType() {
		return PersistentType.TEXT;
	}
	
	@Override
	protected Object convertSingleConnectorPropertyValue(IcConfigurationProperty propertyConfiguration, IdmFormValueDto formValue) {
		if (formValue == null) {
			return null;
		}
		Serializable value = formService.getConfidentialPersistentValue(formValue);
		if (value == null) {
			return null;
		}
		return new org.identityconnectors.common.security.GuardedByteArray(value.toString().getBytes(Charsets.UTF_8));
	}
}
