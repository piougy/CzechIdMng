package eu.bcvsolutions.idm.eav.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Max;
import javax.validation.constraints.Size;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Super class for "extended" attribute values, which can be added to custom abstract entity
 * 
 * @author Radek Tomiška
 *
 * @param <E> Owner entity class
 */
@MappedSuperclass
public abstract class IdmFormAttributeValue<E extends FormableEntity> extends AbstractEntity {

	private static final long serialVersionUID = -5914285774914667917L;

	@ManyToOne(optional = false) // TODO: should we support values without definition?
	@JoinColumn(name = "attribute_definition_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private IdmFormAttributeDefinition attributeDefinition;
	
	@Size(max = DefaultFieldLengths.LOG)
	@Column(name = "string_value", nullable = true, length = DefaultFieldLengths.LOG)
	private String stringValue;
	
	// TODO: other data types
	
	@Max(99999)
	@Column(name = "seq")
	private int seq;
	
	/**
	 * Returns entity, for witch is this attribute value
	 * 
	 * @return
	 */
	public abstract E getOwner();

	/**
	 * Attribute definition
	 * 
	 * @return
	 */
	public IdmFormAttributeDefinition getAttributeDefinition() {
		return attributeDefinition;
	}

	public void setAttributeDefinition(IdmFormAttributeDefinition attributeDefinition) {
		this.attributeDefinition = attributeDefinition;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}
}
