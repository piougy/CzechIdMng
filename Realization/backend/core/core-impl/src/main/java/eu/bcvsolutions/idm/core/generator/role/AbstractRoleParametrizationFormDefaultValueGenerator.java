package eu.bcvsolutions.idm.core.generator.role;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmGenerateValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFormAttributeFilter;
import eu.bcvsolutions.idm.core.api.generator.AbstractValueGenerator;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;

/**
 * Abstract class for generators that use generating EAV values for role parameterization.
 *
 * @author Ondrej Kopr
 * @since 9.4.0
 */
public abstract class AbstractRoleParametrizationFormDefaultValueGenerator<DTO extends AbstractDto>
		extends AbstractValueGenerator<DTO> {

	public static String REGEX_MULTIPLE_VALUES = "regexMultipleValues";
	private static String REGEX_MULTIPLE_VALUES_DEFAULT_VALUE = ",";

	@Autowired
	protected IdmFormDefinitionService formDefinitionService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmRoleFormAttributeService roleFormAttributeService;

	protected IdmFormInstanceDto getDefaultValuesByRoleDefinition(IdmRoleDto roleDto,
			IdmGenerateValueDto valueGenerator, IdmFormInstanceDto existingEavs, DTO dto) {
		UUID formDefinitionId = roleDto.getIdentityRoleAttributeDefinition();

		// If form definition doesn't exist continue as normal
		if (formDefinitionId == null) {
			return null;
		}

		IdmFormDefinitionDto definition = roleService.getFormAttributeSubdefinition(roleDto);
		// Internal behavior #getValuesFromInstances wants default form value

		List<IdmFormValueDto> values = new ArrayList<>();
		for (IdmFormAttributeDto attribute : definition.getFormAttributes()) {
			String defaultValue = getDefaultValue(attribute, roleDto);
			// check if exists default values
			if (StringUtils.isEmpty(defaultValue)) {

				// Check given values
				List<IdmFormValueDto> existingValues = existingEavs == null ? null : existingEavs.getValues().stream() //
						.filter(existingEav -> { //
							return existingEav.getFormAttribute().equals(attribute.getId());
						}).collect(Collectors.toList()); //
				if (existingValues != null && !existingValues.isEmpty()) {
					// Given value exist and default value doesn't exist
					values.addAll(existingValues);
				}
				continue;
			}

			if (valueGenerator.isRegenerateValue()) {
				// For regenerate just create values by default values
				String regex = this.getRegex(valueGenerator);
				String[] defaultValues = defaultValue.split(regex);
				for (String defValue : defaultValues) {
					IdmFormValueDto value = new IdmFormValueDto(attribute);
					value.setValue(defValue);
					values.add(value);
				}
			} else {
				if (existingEavs != null) {
					// With check existing values
					List<IdmFormValueDto> existingValues = existingEavs.getValues()
							.stream() //
							.filter(existingVal -> { //
								return existingVal.getFormAttribute().equals(attribute.getId()); //
							}) //
							.collect(Collectors.toList()); //
					if (!existingValues.isEmpty()) {
						// If existing value isn't empty use it (given values)
						existingValues.forEach(existingValue -> {
							IdmFormValueDto value = new IdmFormValueDto(attribute);
							value.setValue(existingValue.getValue(attribute.getPersistentType()));
							values.add(value);
						});
						continue;
					}
				}
				// If given values is empty use default
				if (attribute.isMultiple()) {
					// Default value may be multiple, just iterate over value splited by regex
					String regex = this.getRegex(valueGenerator);
					String[] defaultValues = defaultValue.split(regex);
					for (String defValue : defaultValues) {
						IdmFormValueDto value = new IdmFormValueDto(attribute);
						value.setValue(defValue);
						values.add(value);
					}
				} else {
					// If is value single valued use it as is it
					IdmFormValueDto value = new IdmFormValueDto(attribute);
					value.setValue(defaultValue);
					values.add(value);
				}
				
			}
		}

		return new IdmFormInstanceDto(dto, definition, values);
	}

	/**
	 * Return default value for given form-attribute. First try to find override
	 * role-form-attribute. If exists, then return it's default value. If not, then
	 * default value from given form-attribute will be returned.
	 * 
	 * @param attribute
	 * @param role
	 * @return
	 */
	private String getDefaultValue(IdmFormAttributeDto attribute, IdmRoleDto role) {
		Assert.notNull(attribute, "Attribute is required.");
		Assert.notNull(role, "Role is required.");

		IdmRoleFormAttributeFilter roleFormAttributeFilter = new IdmRoleFormAttributeFilter();
		roleFormAttributeFilter.setRole(role.getId());
		roleFormAttributeFilter.setFormAttribute(attribute.getId());
		List<IdmRoleFormAttributeDto> roleFormAttributes = roleFormAttributeService.find(roleFormAttributeFilter, null)
				.getContent();
		if (roleFormAttributes.size() > 0) {
			return roleFormAttributes.get(0).getDefaultValue();
		}

		return attribute.getDefaultValue();
	}

	/**
	 * Get regex
	 *
	 * @param valueGenerator
	 * @return
	 */
	protected String getRegex(IdmGenerateValueDto valueGenerator) {
		String regex = valueGenerator.getGeneratorProperties().getString(REGEX_MULTIPLE_VALUES);
		if (StringUtils.isEmpty(regex)) {
			return REGEX_MULTIPLE_VALUES_DEFAULT_VALUE;
		}
		return regex;
	}

	@Override
	public List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>();
		properties.add(REGEX_MULTIPLE_VALUES);
		return properties;
	}
}
