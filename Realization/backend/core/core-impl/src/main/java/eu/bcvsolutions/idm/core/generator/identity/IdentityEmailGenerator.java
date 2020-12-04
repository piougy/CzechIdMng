package eu.bcvsolutions.idm.core.generator.identity;

import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmGenerateValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * Default core implementation generating username. Generator takes lower firstname,
 * and unite with lower lastname without diacritics.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 * @since 9.2.0
 */
@Component(IdentityEmailGenerator.GENERATOR_NAME)
@Description("Generate idenity email from first name and lastname, or username.")
public class IdentityEmailGenerator extends AbstractIdentityValueGenerator {

	public static final String GENERATOR_NAME = "core-identity-email-value-generator";
	public static final String EMAIL_SUFFIX = "emailSuffix";
	public static final String GENERATE_FROM_USERNAME = "generateFromUsername";
	public static final String PROPERTY_UNIQUE_EMAIL = "unique-email";
	//
	private static Character AT_CONSTANT = '@';
	private static final int MAXIMUM_SEARCH_FOR_UNIQUE_EMAIL = 100;
	//
	@Autowired private IdmIdentityService identityService;
	
	@Override
	public String getName() {
		return GENERATOR_NAME;
	}

	@Override
	public List<String> getPropertyNames() {
		List<String> properties = super.getPropertyNames();
		properties.add(EMAIL_SUFFIX);
		properties.add(GENERATE_FROM_USERNAME);
		properties.add(PROPERTY_UNIQUE_EMAIL);
		return properties;
	}

	@Override
	public IdmIdentityDto generate(IdmIdentityDto dto, IdmGenerateValueDto valueGenerator) {
		Assert.notNull(dto, "DTO is required.");
		Assert.notNull(valueGenerator, "Value generator is required.");
		//		
		// if exists email and configuration doesn't allow regenerate return dto
		if (!valueGenerator.isRegenerateValue() && StringUtils.isNotEmpty(dto.getEmail())) {
			return dto;
		}
		
		String transformedUsername = null;
		if (isGenerateFromUsername(valueGenerator)) {
			transformedUsername = StringUtils.stripAccents(dto.getUsername());
			transformedUsername = replaceAllWhiteSpaces(valueGenerator, transformedUsername);
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
		String transformedSuffix = getTransformedSuffix(valueGenerator, emailSuffix);
		String email = String.format("%s%s", transformedUsername, transformedSuffix);
		if (isUniqueEmail(valueGenerator)) {
			email = getUniqueEmail(transformedUsername, transformedSuffix);
		}
		//
		dto.setEmail(email);
		//
		return dto;
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
			} else if (attribute.getName().equals(PROPERTY_UNIQUE_EMAIL)) {
				attribute.setPersistentType(PersistentType.BOOLEAN);
				attribute.setDefaultValue(Boolean.TRUE.toString());
			}
		});
		return attributes;
	}
	
	/**
	 * Transform suffix, add AT constant and remove/replace white spaces.
	 *
	 * @param valueGenerator
	 * @param emailSuffix
	 * @return
	 */
	private String getTransformedSuffix(IdmGenerateValueDto valueGenerator, String emailSuffix) {
		StringBuilder result = new StringBuilder();
		if (StringUtils.contains(emailSuffix, AT_CONSTANT)) {
			result.append(emailSuffix);
		} else {
			// defensive behavior
			result.append(AT_CONSTANT);
			result.append(emailSuffix);
		}
		return replaceAllWhiteSpaces(valueGenerator, result.toString());
	}
	/**
	 * Get connection characters
	 *
	 * @return
	 */
	private String getEmailSuffix(IdmGenerateValueDto valueGenerator) {
		return valueGenerator.getGeneratorProperties().getString(EMAIL_SUFFIX);
	}

	/**
	 * Is generated from username
	 *
	 * @param valueGenerator
	 * @return
	 */
	private boolean isGenerateFromUsername(IdmGenerateValueDto valueGenerator) {
		return BooleanUtils.toBoolean(valueGenerator.getGeneratorProperties().getBoolean(GENERATE_FROM_USERNAME));
	}
	
	/**
	 * Unique email has to be generated.
	 *
	 * @return unique email
	 * @since 10.7.0
	 */
	private boolean isUniqueEmail(IdmGenerateValueDto valueGenerator) {
		return valueGenerator.getGeneratorProperties().getBoolean(PROPERTY_UNIQUE_EMAIL);
	}
	
	/**
	 * Method returns unique username. Try found this username and increment it.
	 *
	 * @param username
	 * @return
	 */
	private String getUniqueEmail(String transformedUsername, String transformedSuffix) {
		IdmIdentityFilter filter = new IdmIdentityFilter();
		String email = String.format("%s%s", transformedUsername, transformedSuffix);
		filter.setEmail(email);
		long count = identityService.count(filter);
		if (count == 0) {
			return email;
		}
		//
		for (int index = 1; index < MAXIMUM_SEARCH_FOR_UNIQUE_EMAIL; index++) {
			email = String.format("%s%s%s", transformedUsername, index, transformedSuffix);
			filter.setEmail(email);
			if (identityService.count(filter) == 0) {
				break;
			}
		}
		//
		return email;
	}
}
