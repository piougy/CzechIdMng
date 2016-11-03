package eu.bcvsolutions.idm.eav.entity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Form definition for different entity / object types 
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_form_definition", indexes = { @Index(name = "ux_idm_form_definition_name", columnList = "name", unique = true) })
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class IdmFormDefinition extends AbstractEntity {

	private static final long serialVersionUID = 8267096009610364911L;
	
	@Basic(optional = false)
	@NotNull
	@Size(min = 1, max = 100)
	@Column(name = "name", nullable = false, length = 100)
	private String name; // for entity / object type
	
	@OrderBy("seq")
	@OneToMany(mappedBy = "formDefinition", cascade = CascadeType.REMOVE)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private List<IdmFormAttributeDefinition> formAttributes;
	//
	// attribute definitions cache
	private transient Map<UUID, IdmFormAttributeDefinition> mappedAttributes;
	private transient Map<String, UUID> mappedKeys;

	public IdmFormDefinition() {
	}

	public IdmFormDefinition(UUID id) {
		super(id);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<IdmFormAttributeDefinition> getFormAttributes() {
		if (formAttributes == null) {
			formAttributes = Lists.newArrayList();
		}
		return formAttributes;
	}

	public void setFormAttributes(List<IdmFormAttributeDefinition> formAttributes) {
		this.formAttributes = formAttributes;
		mappedAttributes = null; // musime refresnout
	}
	
	public void addFormAttribute(IdmFormAttributeDefinition formAttribute) {
		getFormAttributes().add(formAttribute);
		mappedAttributes = null; // musime refresnout
	}

	private Map<UUID, IdmFormAttributeDefinition> getMappedAttributes() {
		if (mappedAttributes == null || mappedKeys == null) {
			mappedAttributes = Maps.newHashMap();
			mappedKeys = Maps.newHashMap();
			for (IdmFormAttributeDefinition attribute : getFormAttributes()) {
				mappedAttributes.put(attribute.getId(), attribute);
				mappedKeys.put(attribute.getName(), attribute.getId());
			}
		}
		return mappedAttributes;
	}

	private Map<String, UUID> getMappedNames() {
		if (mappedAttributes == null || mappedKeys == null) {
			getMappedAttributes();
		}
		return mappedKeys;
	}

	/**
	 * Vrati definici atributu dle identifikatoru
	 *
	 * @param formAttributeId
	 * @return
	 */
	public IdmFormAttributeDefinition getMappedAttribute(Long formAttributeId) {
		return getMappedAttributes().get(formAttributeId);
	}

	/**
	 * Vrati definici atributu dle klice
	 *
	 * @param formAttributeKey
	 * @return
	 */
	public IdmFormAttributeDefinition getMappedAttributeByKey(String formAttributeKey) {
		if (!getMappedNames().containsKey(formAttributeKey)) {
			return null;
		}
		return getMappedAttributes().get(getMappedNames().get(formAttributeKey));
	}
}
