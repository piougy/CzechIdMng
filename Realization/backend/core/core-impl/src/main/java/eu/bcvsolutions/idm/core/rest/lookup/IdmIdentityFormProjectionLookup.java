package eu.bcvsolutions.idm.core.rest.lookup;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.lookup.AbstractFormProjectionLookup;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormProjectionService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;

/**
 * Identity form projection lookup.
 * 
 * @author Radek Tomiška
 * @since 11.0.0
 */
@Component
public class IdmIdentityFormProjectionLookup extends AbstractFormProjectionLookup<IdmIdentityDto> {

	@Autowired private ApplicationContext applicationContext;
	//
	private IdmFormProjectionService formProjectionService;
	
	@Override
	public IdmFormProjectionDto lookupProjection(IdmIdentityDto dto) {
		Assert.notNull(dto, "Dto is required to find form projection");
		//
		UUID formProjection = dto.getFormProjection();
		if (formProjection == null) {
			return null;
		}
		return getFormProjectionService().get(formProjection);
	}
	
	@Override
	public IdmFormInstanceDto lookupBasicFieldsInstance(IdmIdentityDto dto) {
		IdmFormDefinitionDto basicFieldsDefinition = getBasicFieldsDefinition(dto);
		if (basicFieldsDefinition == null) {
			return null;
		}
		// filter identity attributes from projection
		basicFieldsDefinition.setFormAttributes(
				basicFieldsDefinition
					.getFormAttributes()
					.stream()
					.filter(attribute -> {
						String attributeName = attribute.getName();
						//
						return StringUtils.isEmpty(attributeName) 
								|| attributeName.startsWith(String.format("%s.", IdmIdentity.class.getSimpleName()));
					})
					.collect(Collectors.toList())
		);
		// fill projection values by given dto
		ArrayList<IdmFormValueDto> values = new ArrayList<>(basicFieldsDefinition.getFormAttributes().size());
		// username
		appendAttributeValue(values, basicFieldsDefinition, IdmIdentity_.username.getName(), dto.getUsername());
		// first name
		appendAttributeValue(values, basicFieldsDefinition, IdmIdentity_.firstName.getName(), dto.getFirstName());
		// last name
		appendAttributeValue(values, basicFieldsDefinition, IdmIdentity_.lastName.getName(), dto.getLastName());
		// personal number
		appendAttributeValue(values, basicFieldsDefinition, IdmIdentity_.externalCode.getName(), dto.getExternalCode());
		// phone
		appendAttributeValue(values, basicFieldsDefinition, IdmIdentity_.phone.getName(), dto.getPhone());
		// email
		appendAttributeValue(values, basicFieldsDefinition, IdmIdentity_.email.getName(), dto.getEmail());
		// title before
		appendAttributeValue(values, basicFieldsDefinition, IdmIdentity_.titleBefore.getName(), dto.getTitleBefore());
		// title after
		appendAttributeValue(values, basicFieldsDefinition, IdmIdentity_.titleAfter.getName(), dto.getTitleAfter());
		// description
		appendAttributeValue(values, basicFieldsDefinition, IdmIdentity_.description.getName(), dto.getDescription());
		//
		return new IdmFormInstanceDto(dto, basicFieldsDefinition, values);
	}
	
	private  IdmFormProjectionService getFormProjectionService() {
		if (formProjectionService == null) {
			formProjectionService = applicationContext.getBean(IdmFormProjectionService.class);
		}
		return formProjectionService;
	}
}
