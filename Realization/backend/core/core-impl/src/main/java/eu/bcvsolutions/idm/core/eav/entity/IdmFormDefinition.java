package eu.bcvsolutions.idm.core.eav.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.UnmodifiableEntity;

/**
 * Form definition for different entity / object types 
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_form_definition", indexes = { @Index(name = "ux_idm_form_definition_tn", columnList = "definition_type,name", unique = true) })
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class IdmFormDefinition extends AbstractEntity implements UnmodifiableEntity {

	private static final long serialVersionUID = 8267096009610364911L;
	
	@NotNull
	@Audited
	@Basic(optional = false)	
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "definition_type", nullable = false, length = DefaultFieldLengths.NAME)
	private String type; // for entity / object type
	
	@NotNull
	@Audited
	@Basic(optional = false)
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", nullable = false, length = DefaultFieldLengths.NAME)
	private String name; // unique name for entity / object type
	
	@Audited
	@OrderBy("seq")
	@OneToMany(mappedBy = "formDefinition", fetch = FetchType.EAGER)
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private List<IdmFormAttribute> formAttributes;
	
	@NotNull
	@Column(name = "unmodifiable", nullable = false)
	private boolean unmodifiable = false;
	//
	// attribute definitions cache
	private transient Map<UUID, IdmFormAttribute> mappedAttributes;
	private transient Map<String, Serializable> mappedKeys;

	public IdmFormDefinition() {
	}
	
	/**
	 * Form definition for entity / object type
	 * 
	 * @return
	 */
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Unique name for entity / object type
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns defined form attributes. Returns empty list, when no attribute is defined.
	 * 
	 * @return
	 */
	public List<IdmFormAttribute> getFormAttributes() {
		if (formAttributes == null) {
			formAttributes = Lists.newArrayList();
		}
		return formAttributes;
	}

	public void setFormAttributes(List<IdmFormAttribute> formAttributes) {
		this.formAttributes = formAttributes;
		mappedAttributes = null; // musime refresnout
	}
	
	public void addFormAttribute(IdmFormAttribute formAttribute) {
		getFormAttributes().add(formAttribute);
		mappedAttributes = null; // musime refresnout
	}

	/**
	 * Returns defined attributes as map
	 * 
	 * @return
	 */
	private Map<UUID, IdmFormAttribute> getMappedAttributes() {
		if (mappedAttributes == null || mappedKeys == null) {
			mappedAttributes = Maps.newHashMap();
			mappedKeys = Maps.newHashMap();
			for (IdmFormAttribute attribute : getFormAttributes()) {
				mappedAttributes.put(attribute.getId(), attribute);
				mappedKeys.put(attribute.getName(), attribute.getId());
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
	public IdmFormAttribute getMappedAttribute(UUID formAttributeId) {
		return getMappedAttributes().get(formAttributeId);
	}

	/**
	 * Returns attribute definition by name
	 *
	 * @param attributeName
	 * @return
	 */
	public IdmFormAttribute getMappedAttributeByName(String attributeName) {
		if (!getMappedNames().containsKey(attributeName)) {
			return null;
		}
		return getMappedAttributes().get(getMappedNames().get(attributeName));
	}

	@Override
	public boolean isUnmodifiable() {
		return this.unmodifiable;
	}

	@Override
	public void setUnmodifiable(boolean unmodifiable) {
		this.unmodifiable = unmodifiable;
	}
}
