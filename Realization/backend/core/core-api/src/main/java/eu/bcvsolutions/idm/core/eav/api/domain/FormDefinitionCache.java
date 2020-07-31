package eu.bcvsolutions.idm.core.eav.api.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;

/**
 * Cached form definitions - we need to get main and definition by code and id.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
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
	 * @param original
	 * @return
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
		clone.setFormAttributes(Lists.newArrayList(original.getFormAttributes()));
		//
		return clone;
	}
}
