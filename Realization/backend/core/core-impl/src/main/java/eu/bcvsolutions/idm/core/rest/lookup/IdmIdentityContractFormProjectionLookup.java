package eu.bcvsolutions.idm.core.rest.lookup;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;
import eu.bcvsolutions.idm.core.api.rest.lookup.AbstractFormProjectionLookup;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;

/**
 * Lookup projection for contract.
 * 
 * @author Radek Tomiška
 * @since 11.0.0
 */
@Component
public class IdmIdentityContractFormProjectionLookup extends AbstractFormProjectionLookup<IdmIdentityContractDto> {

	@Autowired private IdmIdentityDtoLookup identityLookup;
	@Autowired private IdmIdentityFormProjectionLookup identityFormProjectionLookup;

	@Override
	public IdmFormProjectionDto lookupProjection(IdmIdentityContractDto dto) {
		Assert.notNull(dto, "Dto is required to find form projection");
		//
		UUID identityId = dto.getIdentity();
		if (identityId == null) {
			return null;
		}
		IdmIdentityDto identity = identityLookup.lookup(identityId);
		if (identity == null) {
			return null;
		}
		//
		return identityFormProjectionLookup.lookupProjection(identity);
	}
	
	@Override
	public IdmFormInstanceDto lookupBasicFieldsInstance(IdmIdentityContractDto dto) {
		IdmFormDefinitionDto basicFieldsDefinition = getBasicFieldsDefinition(dto);
		if (basicFieldsDefinition == null) {
			return null;
		}
		// filter contract attributes 
		basicFieldsDefinition.setFormAttributes(
				basicFieldsDefinition
					.getFormAttributes()
					.stream()
					.filter(attribute -> {
						String attributeName = attribute.getName();
						//
						return StringUtils.isNotEmpty(attributeName) 
								&& attributeName.startsWith(String.format("%s.", IdmIdentityContract.class.getSimpleName()));
					})
					.collect(Collectors.toList())
		);
		//
		// fill projection values by given dto
		ArrayList<IdmFormValueDto> values = new ArrayList<>(basicFieldsDefinition.getFormAttributes().size());
		// work position
		appendAttributeValue(values, basicFieldsDefinition, IdmIdentityContractDto.PROPERTY_WORK_POSITION, dto.getWorkPosition());
		// valid from
		appendAttributeValue(values, basicFieldsDefinition, ValidableEntity.PROPERTY_VALID_FROM, dto.getValidFrom());
		// valid till
		appendAttributeValue(values, basicFieldsDefinition, ValidableEntity.PROPERTY_VALID_TILL, dto.getValidTill());
		//
		return new IdmFormInstanceDto(dto, basicFieldsDefinition, values);
	}
}
