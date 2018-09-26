package eu.bcvsolutions.idm.core.generator.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.dto.IdmGeneratedValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.generator.AbstractValueGenerator;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
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
@Description("Generate idenity username from firstName and lastName.")
public class IdentityUsernameGenerator extends AbstractValueGenerator<IdmIdentityDto> {

	public static String FIRST_NAME_CHARACTERS_COUNT = "firstNameCharacterCount";
	public static String LAST_NAME_CHARACTERS_COUNT = "lastNameCharacterCount";
	public static String CONNECTING_CHARACTER = "connectionCharacter";
	public static String SEARCH_UNIQUE_USERNAME = "searchUniqueUsername";

	private static int MAXIMUM_SEARCH_FOR_UNIQUE_USERNAME = 100;

	@Autowired
	private IdmIdentityService identityService;

	@Override
	public List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>();
		properties.add(CONNECTING_CHARACTER);
		properties.add(FIRST_NAME_CHARACTERS_COUNT);
		properties.add(LAST_NAME_CHARACTERS_COUNT);
		properties.add(SEARCH_UNIQUE_USERNAME);
		return properties;
	}

	@Override
	protected IdmIdentityDto generateItem(IdmIdentityDto dto, IdmGeneratedValueDto valueGenerator) {
		// if exists username and configuration doesn't allow regenerate return dto
		if (!valueGenerator.isRegenerateValue() && StringUtils.isNotEmpty(dto.getUsername())) {
			return dto;
		}
		String transformedFirstName = StringUtils.stripAccents(StringUtils.trimToEmpty(dto.getFirstName()));
		String transformedLastName = StringUtils.stripAccents(StringUtils.trimToEmpty(dto.getLastName()));
		//
		if (StringUtils.isEmpty(transformedFirstName) || StringUtils.isEmpty(transformedLastName)) {
			// firstname and lastname is required
			return dto;
		}
		//
		Integer firstNameCharacterCount = this.getFirstNameCharacterCount(valueGenerator);
		if (firstNameCharacterCount != null) {
			transformedFirstName = StringUtils.substring(transformedFirstName, 0, firstNameCharacterCount);
		}
		//
		Integer lastNameCharacterCount = this.getLastNameCharacterCount(valueGenerator);
		if (lastNameCharacterCount != null) {
			transformedLastName = StringUtils.substring(transformedLastName, 0, lastNameCharacterCount);
		}
		//
		StringBuilder result = new StringBuilder();
		result.append(transformedFirstName);
		String connectingCharacter = this.getConnectingCharacter(valueGenerator);
		if (connectingCharacter != null) {
			result.append(connectingCharacter);
		}
		result.append(transformedLastName);
		//
		// username has more character than accept IdM
		String resultUsername = result.toString().toLowerCase();
		if (resultUsername.length() > DefaultFieldLengths.NAME) {
			// TODO: found better solution
			resultUsername = resultUsername.substring(0, DefaultFieldLengths.NAME);
		}
		//
		if (isSearchUniqueUsername(valueGenerator)) {
			dto.setUsername(getUniqueUsername(resultUsername));
		} else {
			dto.setUsername(resultUsername);
		}
		//
		return dto;
	}

	@Override
	public Class<? extends AbstractEntity> getEntityClass() {
		return IdmIdentity.class;
	}

	/**
	 * Method returns unique username. Try found this username and increment it.
	 *
	 * @param username
	 * @return
	 */
	private String getUniqueUsername(String username) {
		IdmIdentityFilter filter = new IdmIdentityFilter();
		
		filter.setUsername(username);
		long count = identityService.count(filter);
		if (count > 0) {
			for (int index = 1; index < MAXIMUM_SEARCH_FOR_UNIQUE_USERNAME; index++) {
				String newUsername = username + index;
				filter.setUsername(newUsername);
				count = identityService.count(filter);
				if (count == 0) {
					// founded free
					return newUsername;
				}
			}
		}
		// not founded return old
		return username;
	}

	/**
	 * Get connection characters
	 *
	 * @return
	 */
	private String getConnectingCharacter(IdmGeneratedValueDto valueGenerator) {
		return valueGenerator.getGeneratorProperties().getString(CONNECTING_CHARACTER);
	}

	/**
	 * Get firstName characters length
	 *
	 * @return
	 */
	private Integer getFirstNameCharacterCount(IdmGeneratedValueDto valueGenerator) {
		Object value = valueGenerator.getGeneratorProperties().getOrDefault(FIRST_NAME_CHARACTERS_COUNT, null);
		if (value == null) {
			return null;
		}
		return new Integer(value.toString());
	}

	/**
	 * Get lastName characters length
	 *
	 * @return
	 */
	private Integer getLastNameCharacterCount(IdmGeneratedValueDto valueGenerator) {
		Object value = valueGenerator.getGeneratorProperties().getOrDefault(LAST_NAME_CHARACTERS_COUNT, null);
		if (value == null) {
			return null;
		}
		return new Integer(value.toString());
	}

	/**
	 * Is search unique username
	 *
	 * @return
	 */
	private boolean isSearchUniqueUsername(IdmGeneratedValueDto valueGenerator) {
		return BooleanUtils.toBoolean(valueGenerator.getGeneratorProperties().getBoolean(SEARCH_UNIQUE_USERNAME));
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> attributes = super.getFormAttributes();
		attributes.forEach(attribute -> {
			if (attribute.getName().equals(LAST_NAME_CHARACTERS_COUNT)) {
				attribute.setPersistentType(PersistentType.INT);
			} else if (attribute.getName().equals(FIRST_NAME_CHARACTERS_COUNT)) {
				attribute.setPersistentType(PersistentType.INT);
			} else if (attribute.getName().equals(SEARCH_UNIQUE_USERNAME)) {
				attribute.setPersistentType(PersistentType.BOOLEAN);
			}
		});
		return attributes;
	}
}
