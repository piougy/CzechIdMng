package eu.bcvsolutions.idm.core.generator.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmGeneratedValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.generator.AbstractValueGenerator;
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
@Description("Generate idenity email from username.")
public class IdentityEmailGenerator extends AbstractValueGenerator<IdmIdentityDto> {

	public static String EMAIL_SUFFIX = "emailSuffix";

	private static Character AT_CONSTANT = '@';

	@Override
	public List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>();
		properties.add(EMAIL_SUFFIX);
		return properties;
	}

	@Override
	protected IdmIdentityDto generateItem(IdmIdentityDto dto, IdmGeneratedValueDto valueGenerator) {
		// if exists email and configuration doesn't allow regenerate return dto
		if (!valueGenerator.isRegenerateValue() && StringUtils.isNotEmpty(dto.getEmail())) {
			return dto;
		}
		String transformedUsername = StringUtils.stripAccents(StringUtils.trimToEmpty(dto.getUsername()));
		//
		if (StringUtils.isEmpty(transformedUsername) ) {
			// username is required
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
		if (StringUtils.contains(emailSuffix, AT_CONSTANT)) {
			result.append(emailSuffix);
		} else {
			// defensive behavior
			result.append(AT_CONSTANT);
			result.append(emailSuffix);
		}
		dto.setEmail(result.toString());
		//
		return dto;
	}

	@Override
	public Class<? extends AbstractEntity> getEntityClass() {
		return IdmIdentity.class;
	}

	/**
	 * Get connection characters
	 *
	 * @return
	 */
	private String getEmailSuffix(IdmGeneratedValueDto valueGenerator) {
		return valueGenerator.getGeneratorProperties().getString(EMAIL_SUFFIX);
	}

	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> attributes = super.getFormAttributes();
		attributes.forEach(attribute -> {
			if (attribute.getName().equals(EMAIL_SUFFIX)) {
				attribute.setRequired(true);
			}
		});
		return attributes;
	}
}
