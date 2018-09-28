package eu.bcvsolutions.idm.core.generator.impl;

import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmGeneratedValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Default core implementation generating username. Generator takes lower firstname,
 * and unite with lower lastname without diacritics.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component
@Description("Generate idenity email from first name and lastname, or username.")
public class IdentityEmailGenerator extends AbstractIdentityValueGenerator {

	public static String EMAIL_SUFFIX = "emailSuffix";
	public static String GENERATE_FROM_USERNAME = "generateFromUsername";

	private static Character AT_CONSTANT = '@';

	@Override
	public List<String> getPropertyNames() {
		List<String> properties = super.getPropertyNames();
		properties.add(EMAIL_SUFFIX);
		properties.add(GENERATE_FROM_USERNAME);
		return properties;
	}

	@Override
	protected IdmIdentityDto generateItem(IdmIdentityDto dto, IdmGeneratedValueDto valueGenerator) {
		// if exists email and configuration doesn't allow regenerate return dto
		if (!valueGenerator.isRegenerateValue() && StringUtils.isNotEmpty(dto.getEmail())) {
			return dto;
		}
		
		String transformedUsername = null;
		if (isGenerateFromUsername(valueGenerator)) {
			transformedUsername = StringUtils.stripAccents(StringUtils.trimToEmpty(dto.getUsername()));
		} else {
			transformedUsername = super.generateUsername(dto, valueGenerator);
		}
		//
		if (StringUtils.isEmpty(transformedUsername) ) {
			// transformed username is required
			return dto;
		}
		//
		if (StringUtils.contains(transformedUsername, AT_CONSTANT)) {
			// username contains @ invalid mail return whole dto
			return dto;
		}
		String emailSuffix = getEmailSuffix(valueGenerator);
		if (StringUtils.isEmpty(emailSuffix)) {
			// email suffix is empty
			return dto;
		}
		//
		StringBuilder result = new StringBuilder();
		result.append(transformedUsername);
		result.append(getTransformedSuffix(emailSuffix));
		
		dto.setEmail(result.toString());
		//
		return dto;
	}

	@Override
	public Class<? extends AbstractEntity> getEntityClass() {
		return IdmIdentity.class;
	}

	private String getTransformedSuffix(String emailSuffix) {
		StringBuilder result = new StringBuilder();
		if (StringUtils.contains(emailSuffix, AT_CONSTANT)) {
			result.append(emailSuffix);
		} else {
			// defensive behavior
			result.append(AT_CONSTANT);
			result.append(emailSuffix);
		}
		return result.toString();
	}
	/**
	 * Get connection characters
	 *
	 * @return
	 */
	private String getEmailSuffix(IdmGeneratedValueDto valueGenerator) {
		return valueGenerator.getGeneratorProperties().getString(EMAIL_SUFFIX);
	}

	/**
	 * Is generated from username
	 *
	 * @param valueGenerator
	 * @return
	 */
	private boolean isGenerateFromUsername(IdmGeneratedValueDto valueGenerator) {
		return BooleanUtils.toBoolean(valueGenerator.getGeneratorProperties().getBoolean(GENERATE_FROM_USERNAME));
	}

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> attributes = super.getFormAttributes();
		attributes.forEach(attribute -> {
			if (attribute.getName().equals(EMAIL_SUFFIX)) {
				attribute.setRequired(true);
			} else if (attribute.getName().equals(GENERATE_FROM_USERNAME)) {
				attribute.setPersistentType(PersistentType.BOOLEAN);
				attribute.setDefaultValue(Boolean.FALSE.toString());
			}
		});
		return attributes;
	}
}
