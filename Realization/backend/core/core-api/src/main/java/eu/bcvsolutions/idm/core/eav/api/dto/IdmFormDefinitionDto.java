package eu.bcvsolutions.idm.core.eav.api.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.UnmodifiableEntity;

/**
 * Form definition dto
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "formDefinitions")
public class IdmFormDefinitionDto extends AbstractDto implements UnmodifiableEntity, Codeable {

	private static final long serialVersionUID = 1L;
	
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String type; // for entity / object type
	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	private String code;
	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	private String name;
	@NotNull
	private boolean main;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String description;
	@NotNull
	private boolean unmodifiable = false;
	//
	// attribute definitions cache
	private List<IdmFormAttributeDto> formAttributes;
	private transient Map<UUID, IdmFormAttributeDto> mappedAttributes;
	private transient Map<String, Serializable> mappedKeys;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isMain() {
		return main;
	}

	public void setMain(boolean main) {
		this.main = main;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean isUnmodifiable() {
		return unmodifiable;
	}

	@Override
	public void setUnmodifiable(boolean unmodifiable) {
		this.unmodifiable = unmodifiable;
	}
	
	/**
	 * Returns defined form attributes. Returns empty list, when no attribute is defined.
	 * 
	 * @return
	 */
	public List<IdmFormAttributeDto> getFormAttributes() {
		if (formAttributes == null) {
			formAttributes = Lists.newArrayList();
		}
		return formAttributes;
	}

	public void setFormAttributes(List<IdmFormAttributeDto> formAttributes) {
		this.formAttributes = Lists.newArrayList(formAttributes);
		mappedAttributes = null; // musime refresnout
	}
	
	public void addFormAttribute(IdmFormAttributeDto formAttribute) {
		getFormAttributes().add(formAttribute);
		mappedAttributes = null; // musime refresnout
	}

	/**
	 * Returns defined attributes as map
	 * 
	 * @return
	 */
	private Map<UUID, IdmFormAttributeDto> getMappedAttributes() {
		if (mappedAttributes == null || mappedKeys == null) {
			mappedAttributes = Maps.newHashMap();
			mappedKeys = Maps.newHashMap();
			for (IdmFormAttributeDto attribute : getFormAttributes()) {
				mappedAttributes.put(attribute.getId(), attribute);
				mappedKeys.put(attribute.getCode(), attribute.getId());
			}
		}
		return mappedAttributes;
	}

	/**
	 * Return defined attributes by <name, id>
	 * 
	 * @return
	 */
	private Map<String, Serializable> getMappedNames() {
		if (mappedAttributes == null || mappedKeys == null) {
			getMappedAttributes();
		}
		return mappedKeys;
	}

	/**
	 * Returns attribute definition by identifier
	 *
	 * @param formAttributeId
	 * @return
	 */
	public IdmFormAttributeDto getMappedAttribute(UUID formAttributeId) {
		return getMappedAttributes().get(formAttributeId);
	}
	
	/**
	 * Returns attribute definition by code
	 *
	 * @param attributeCode
	 * @return
	 */
	public IdmFormAttributeDto getMappedAttributeByCode(String attributeCode) {
		if (!getMappedNames().containsKey(attributeCode)) {
			return null;
		}
		return getMappedAttributes().get(getMappedNames().get(attributeCode));
	}
}
