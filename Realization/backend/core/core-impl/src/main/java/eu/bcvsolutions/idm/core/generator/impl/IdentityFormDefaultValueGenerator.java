package eu.bcvsolutions.idm.core.generator.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmGenerateValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.generator.AbstractValueGenerator;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;

/**
 * Generator set default values to EAV
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component
@Description("Generator set default values to EAV.")
public class IdentityFormDefaultValueGenerator extends AbstractValueGenerator<IdmIdentityDto> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(IdentityFormDefaultValueGenerator.class);

	public static String FORM_DEFINITION_UUID = "formDefinitionUuid";
	public static String REGEX_MULTIPLE_VALUES = "regexMultipleValues";

	private static String REGEX_MULTIPLE_VALUES_DEFAULT_VALUE = ",";

	@Autowired
	private IdmFormDefinitionService formDefinitionService;
	@Autowired
	private IdmFormAttributeService formAttributeService;

	@Override
	protected IdmIdentityDto generateItem(IdmIdentityDto dto, IdmGenerateValueDto valueGenerator) {
		IdmFormDefinitionDto formDefinition = getFormDefinition(valueGenerator);

		// if is set form definition must by for IdmIdentity type
		if (formDefinition != null && !formDefinition.getType().equals(getDtoClass().getCanonicalName())) {
			LOG.error("Given form definition isn't for entity!");
			throw new ResultCodeException(CoreResultCode.GENERATOR_FORM_DEFINITION_BAD_TYPE,
					ImmutableMap.of(
							"formDefinitionId", formDefinition.getId(),
							"dtoType", getDtoClass().getCanonicalName()));
		}

		List<IdmFormDefinitionDto> formDefinitions = new ArrayList<>();
		if (formDefinition != null) {
			formDefinitions.add(formDefinition);
		} else {
			formDefinitions.addAll(formDefinitionService.findAllByType(getDtoClass().getCanonicalName()));
		}

		List<IdmFormInstanceDto> eavs = dto.getEavs();
		for (IdmFormDefinitionDto definition : formDefinitions) {
			List<IdmFormAttributeDto> attributes = findAllAttributes(definition);
			for (IdmFormAttributeDto att : attributes) {
				// check if exists default values
				if (StringUtils.isEmpty((att.getDefaultValue()))) {
					continue;
				}
				List<IdmFormValueDto> valuesFromInstances = getValuesFromInstances(eavs, definition, att);

				if (valuesFromInstances.isEmpty()) {
					// values are empty, just add all
					valuesFromInstances.addAll(getTransformedValues(valueGenerator, att.getDefaultValue(), att));
				} else {
					// some value exists, check regenerate
					if (valueGenerator.isRegenerateValue()) {
						// replace values
						valuesFromInstances = getTransformedValues(valueGenerator, att.getDefaultValue(), att);
					}
				}

				// replace values in eavs
				eavs = replaceValuesInFormInstance(valuesFromInstances, eavs, definition, att, dto);
			}
		}

		dto.setEavs(eavs);
		return dto;
	}

	@Override
	public List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>();
		properties.add(FORM_DEFINITION_UUID);
		properties.add(REGEX_MULTIPLE_VALUES);
		return properties;
	}

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> attributes = super.getFormAttributes();
		attributes.forEach(attribute -> {
			String attributeName = attribute.getName();
			if (attributeName.equals(FORM_DEFINITION_UUID)) {
				attribute.setPersistentType(PersistentType.UUID);
				attribute.setFaceType(BaseFaceType.FORM_DEFINITION_SELECT);
			}
		});
		return attributes;
	}

	/**
	 * Find all attributes for given {@link IdmFormDefinitionDto}
	 *
	 * @param formDefinitionDto
	 * @return
	 */
	private List<IdmFormAttributeDto> findAllAttributes(IdmFormDefinitionDto formDefinitionDto) {
		IdmFormAttributeFilter filter = new IdmFormAttributeFilter();
		filter.setDefinitionId(formDefinitionDto.getId());
		return formAttributeService.find(filter, null).getContent();
	}
	
	/**
	 * Replace all values in given form instance. Replace will be done for given definition and attribute
	 *
	 * @param replaceValues
	 * @param eavs
	 * @param definition
	 * @param attribute
	 * @return
	 */
	private List<IdmFormInstanceDto> replaceValuesInFormInstance(List<IdmFormValueDto> replaceValues, List<IdmFormInstanceDto> eavs, IdmFormDefinitionDto definition, IdmFormAttributeDto attribute, IdmIdentityDto identityDto) {
		if (replaceValues.isEmpty()) {
			return eavs;
		}
		Optional<IdmFormInstanceDto> foundedInstance = eavs.stream().filter(eav -> eav.getFormDefinition().getId().equals(definition.getId())).findFirst();
		if (foundedInstance.isPresent()) {
			IdmFormInstanceDto instanceDto = foundedInstance.get();
			eavs.remove(instanceDto);
			
			// remove all attributes that is equal to given attribute
			instanceDto.getValues().removeIf(value -> value.getFormAttribute().equals(attribute.getId()));
			instanceDto.getValues().addAll(replaceValues);
			eavs.add(instanceDto);
		} else {
			// create new instance
			eavs.add(new IdmFormInstanceDto(identityDto, definition, replaceValues));
		}
		return eavs;
	}
	
	/**
	 * Transform values to {@link List} of {@link IdmFormValueDto}
	 *
	 * @param valueGenerator
	 * @param value
	 * @param attribute
	 * @return
	 */
	private List<IdmFormValueDto> getTransformedValues(IdmGenerateValueDto valueGenerator, String value, IdmFormAttributeDto attribute) {
		List<IdmFormValueDto> result = new ArrayList<>();
		if (attribute.isMultiple()) {
			String regex = getRegex(valueGenerator);
			for (String splitedValue : value.split(regex)) {
				result.add(createValue(attribute, splitedValue));
			}
		} else {
			result.add(createValue(attribute, value));
		}
		return result;
	}

	/**
	 * Create {@link IdmFormValueDto} by given attribute and given value
	 *
	 * @param attribute
	 * @param value
	 * @return
	 */
	private IdmFormValueDto createValue(IdmFormAttributeDto attribute, Serializable value) {
		IdmFormValueDto formValueDto = new IdmFormValueDto(attribute);
		formValueDto.setValue(value);
		return formValueDto;
	}

	/**
	 * Get form definition from parameters. If definition is not found or is null return null.
	 *
	 * @param valueGenerator
	 * @return
	 */
	private IdmFormDefinitionDto getFormDefinition(IdmGenerateValueDto valueGenerator) {
		UUID formDefinitionUuid = getFormDefinitionUuid(valueGenerator);
		if (formDefinitionUuid != null) {
			IdmFormDefinitionDto formDefinitionDto = formDefinitionService.get(formDefinitionUuid);
			if (formDefinitionDto != null) {
				return formDefinitionDto;
			}
		}
		return null;
	}

	/**
	 * Return all values founded in given eavs. In Eavs will be searched by given definition and attribute.
	 *
	 * @param eavs
	 * @param definition
	 * @param attribute
	 * @return
	 */
	private List<IdmFormValueDto> getValuesFromInstances(List<IdmFormInstanceDto> eavs, IdmFormDefinitionDto definition, IdmFormAttributeDto attribute) {
		List<IdmFormValueDto> result = new ArrayList<>();
		if (eavs == null) {
			return result;
		}
		
		Optional<IdmFormInstanceDto> first = eavs.stream().filter(eav -> eav.getFormDefinition().getCode() == definition.getCode()).findFirst();

		if (first.isPresent()) {
			IdmFormInstanceDto formInstanceDto = first.get();
			
			List<IdmFormValueDto> values = formInstanceDto.getValues();
			
			if (values == null || values.isEmpty()) {
				return result;
			}

			// find all values for given attribute
			result.addAll(values.stream().filter(value -> value.getFormAttribute().equals(attribute.getId())).collect(Collectors.toList()));
		}

		// if doesn't exist return empty list
		return result;
	}
	
	/**
	 * Get form definition uuid
	 *
	 * @return
	 */
	private UUID getFormDefinitionUuid(IdmGenerateValueDto valueGenerator) {
		return valueGenerator.getGeneratorProperties().getUuid(FORM_DEFINITION_UUID);
	}

	/**
	 * Get regex
	 *
	 * @param valueGenerator
	 * @return
	 */
	private String getRegex(IdmGenerateValueDto valueGenerator) {
		String regex = valueGenerator.getGeneratorProperties().getString(REGEX_MULTIPLE_VALUES);
		if (StringUtils.isEmpty(regex)) {
			return REGEX_MULTIPLE_VALUES_DEFAULT_VALUE;
		}
		return regex;
	}
}
