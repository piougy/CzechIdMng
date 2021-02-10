package eu.bcvsolutions.idm.core.eav.api.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;

/**
 * Cached form definitions - we need to get main and definition by code and id.
 * 
 * @author Radek Tomiška
 * @since 10.4.2
 */
public class FormDefinitionCache implements Serializable {
	
	private static final long serialVersionUID = 1L;
	//
	private IdmFormDefinitionDto main;
	private final Map<String, IdmFormDefinitionDto> byCode = new LinkedHashMap<>(); // sorted definitions
	private final Map<UUID, IdmFormDefinitionDto> byId = new HashMap<>();
	
	public void putDefinition(IdmFormDefinitionDto definition) {
		definition = clone(definition);
		//
		if (definition.isMain()) {
			main = definition;
		}
		byCode.put(definition.getCode(), definition);
		byId.put(definition.getId(), definition);
	}
	
	public void putDefinitions(List<IdmFormDefinitionDto> definitions) {
		definitions.forEach(this::putDefinition);
	}
	
	public IdmFormDefinitionDto getMain() {
		return clone(main);
	}
	
	public IdmFormDefinitionDto getByCode(String code) {
		return clone(byCode.get(code));
	}
	
	public IdmFormDefinitionDto getById(UUID id) {
		return clone(byId.get(id));
	}
	
	public List<IdmFormDefinitionDto> getDefinitions() {
		return byCode
				.values()
				.stream()
				.map(this::clone)
				.collect(Collectors.toList());
	}
	
	/**
	 * Clone cached form definition is needed => we need to prevent form definition instance is modified by logged user permissions.
	 * 
	 * @param original definition
	 * @return cloned definition
	 */
	private IdmFormDefinitionDto clone(IdmFormDefinitionDto original) {
		if (original == null) {
			return null;
		}
		//
		IdmFormDefinitionDto clone = new IdmFormDefinitionDto(original);
		clone.setName(original.getName());
		clone.setCode(original.getCode());
		clone.setType(original.getType());
		clone.setTrimmed(original.isTrimmed());
		clone.setModule(original.getModule());
		clone.setEmbedded(original.getEmbedded());
		clone.setUnmodifiable(original.isUnmodifiable());
		clone.setMain(original.isMain());
		clone.setDescription(original.getDescription());
		clone.setFormAttributes(
				original
					.getFormAttributes()
					.stream()
					.map(this::clone)
					.collect(Collectors.toList())
		);
		//
		return clone;
	}
	
	/**
	 * Clone cached form attribute is needed => we need to prevent form attribute instance is modified by logged user permissions.
	 * 
	 * @param original attribute
	 * @return cloned attribute
	 * @since 10.4.3
	 */
	private IdmFormAttributeDto clone(IdmFormAttributeDto original) {
		if (original == null) {
			return null;
		}
		//
		IdmFormAttributeDto clone = new IdmFormAttributeDto(original);
		clone.setFormDefinition(original.getFormDefinition());
		clone.setName(original.getName());
		clone.setCode(original.getCode());
		clone.setPlaceholder(original.getPlaceholder());
		clone.setFaceType(original.getFaceType());
		clone.setMultiple(original.isMultiple());
		clone.setRequired(original.isRequired());
		clone.setReadonly(original.isReadonly());
		clone.setConfidential(original.isConfidential());
		clone.setSeq(original.getSeq());
		clone.setDefaultValue(original.getDefaultValue());
		clone.setUnique(original.isUnique());
		clone.setMax(original.getMax());
		clone.setMin(original.getMin());
		clone.setRegex(original.getRegex());
		clone.setValidationMessage(original.getValidationMessage());
		clone.setPersistentType(original.getPersistentType());
		clone.setTrimmed(original.isTrimmed());
		clone.setModule(original.getModule());
		clone.setEmbedded(original.getEmbedded());
		clone.setUnmodifiable(original.isUnmodifiable());
		clone.setDescription(original.getDescription());
		clone.setProperties(original.getProperties());
		//
		return clone;
	}
}
