package eu.bcvsolutions.idm.eav.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.joda.time.DateTime;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.eav.api.entity.FormableEntity;

/**
 * Super class for "extended" attribute values, which can be added to custom
 * abstract entity
 * 
 * @author Radek Tomiška
 *
 * @param <O> Owner entity class
 */
@MappedSuperclass
public abstract class AbstractFormValue<O extends FormableEntity> extends AbstractEntity {

	private static final long serialVersionUID = -5914285774914667917L;

	@NotNull
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToOne(optional = false) // TODO: should we support values without definition?
	@JoinColumn(name = "attribute_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmFormAttribute formAttribute;

	@NotNull
	@Audited
	@Enumerated(EnumType.STRING)
	@Column(name = "persistent_type", length = 45, nullable = false)
	@JsonProperty(access = Access.READ_ONLY)
	private PersistentType persistentType;
	
	@NotNull
	@Audited
	@Column(name = "confidential", nullable = false)
	@JsonProperty(access = Access.READ_ONLY)
	private boolean confidential;

	@Audited
	@Type(type = "org.hibernate.type.StringClobType") // TODO: test on oracle/ mysql
	@Column(name = "string_value", nullable = true)
	private String stringValue;

	@Audited
	@Column(name = "boolean_value", nullable = true)
	private Boolean booleanValue;

	@Audited
	@Column(name = "long_value", nullable = true)
	private Long longValue;

	@Audited
	@Column(name = "double_value", nullable = true, precision = 38, scale = 4)
	private BigDecimal doubleValue;

	@Audited
	@Column(name = "date_value")
	private DateTime dateValue;
	
	@Audited
	@Column(name = "byte_value")
	private byte[] byteValue;

	@Audited
	@Max(99999)
	@Column(name = "seq")
	private short seq;

	public AbstractFormValue() {
	}

	public AbstractFormValue(UUID id) {
		super(id);
	}

	public AbstractFormValue(IdmFormAttribute formAttribute) {
		Assert.notNull(formAttribute);
		//
		this.formAttribute = formAttribute;
		this.persistentType = formAttribute.getPersistentType();
		this.confidential = formAttribute.isConfidential();
	}

	/**
	 * Returns entity, for witch is this attribute value
	 * 
	 * @return
	 */
	public abstract O getOwner();
	
	/**
	 * Sets this attribute value owner
	 * 
	 * @param owner
	 */
	public abstract void setOwner(O owner);

	/**
	 * Returns value by persistent type
	 * 
	 * @return
	 */
	@JsonProperty(access = Access.READ_ONLY)
	public Serializable getValue() {
		return getValue(persistentType);
	}

	/**
	 * Returns value by persistent type
	 * 
	 * @param persistentType
	 * @return
	 */
	public Serializable getValue(PersistentType persistentType) {
		Assert.notNull(persistentType);
		//
		switch (persistentType) {
		case INT:
			return longValue == null ? null : longValue.intValue();
		case LONG:
			return longValue;
		case BOOLEAN:
			return booleanValue;
		case DATE:
		case DATETIME:
			return dateValue;
		case DOUBLE:
		case CURRENCY:
			return doubleValue;
		case CHAR: 
			return stringValue == null ? null : stringValue.charAt(0);
		case BYTEARRAY: {
			return byteValue;
		}
		default:
			return stringValue;
		}
	}

	/**
	 * Returns true, if value by persistent type is empty
	 *
	 * @return
	 */
	@JsonProperty(access = Access.READ_ONLY)
	public boolean isEmpty() {
		Assert.notNull(persistentType);
		//
		switch (persistentType) {
			case INT:
			case LONG:
				return longValue == null;
			case BOOLEAN:
				return booleanValue == null;
			case DATE:
			case DATETIME:
				return dateValue == null;
			case DOUBLE:
			case CURRENCY:
				return doubleValue == null;
			case BYTEARRAY: {
				return byteValue == null || byteValue.length == 0;
			}
			default:
				return StringUtils.isEmpty(stringValue);
		}
	}
	
	/**
	 * Returns {@code true}, when value by persistent type is equal. 
	 * Returns {@code false}, when other is null.
	 * Returns {@code false}, when persistent types differs.
	 * 
	 * @param other
	 * @return
	 */
	public boolean isEquals(AbstractFormValue<?> other) {
		if (other == null) {
			return false;
		}
		if (!Objects.equals(persistentType, other.getPersistentType())) {
			return false;
		}
		if (isEmpty() && other.isEmpty()) {
			return true;
		}
		if((isEmpty() && !other.isEmpty()) || (!isEmpty() && other.isEmpty())) {
			return false;
		}
		//
		Assert.notNull(persistentType);
		switch (persistentType) {
			case DATE:
			case DATETIME:
				// date from FE vs DB has different chronology - we are using isEquals method
				return dateValue.isEqual(other.getDateValue());
			default:
				return Objects.equals(getValue(), other.getValue());
		}
	}

	/**
	 * Sets value by persintent type
	 *
	 * @param value
	 */
	public void setValue(Serializable value) {
		Assert.notNull(persistentType);
		//
		switch (persistentType) {
			case INT:
			case LONG:
				if (value == null) {
					setLongValue(null);
				} else if (value instanceof Long) {
					setLongValue((Long) value);
				} else if (value instanceof Integer) {
					setLongValue(((Integer) value).longValue());
				} else if (value instanceof Number) {
					setLongValue(((Number) value).longValue());
				} else {
					throw new IllegalArgumentException(MessageFormat.format("Form value [{0}] has to be [{1}], given [{2}]", 
							formAttribute.getName(), persistentType, value));
				}
				break;
			case BOOLEAN:
				if (value == null) {
					setBooleanValue(null);
				} else if (value instanceof Boolean) {
					setBooleanValue((Boolean) value);
				} else {
					throw new IllegalArgumentException(MessageFormat.format("Form value [{0}] has to be [{1}], given [{2}]", 
							formAttribute.getName(), persistentType, value));
				}
				break;
			case DATE:
			case DATETIME:
				if (value == null) {
					setDateValue(null);
				} else if (value instanceof DateTime) {
					setDateValue((DateTime) value);
				} else if (value instanceof Date) {
					setDateValue(new DateTime((Date) value));
				} else {
					throw new IllegalArgumentException(MessageFormat.format("Form value [{0}] has to be [{1}], given [{2}]", 
							formAttribute.getName(), persistentType, value));
				}
				break;
			case DOUBLE:
			case CURRENCY:
				if (value == null) {
					setDoubleValue(null);
				} else if (value instanceof BigDecimal) {
					setDoubleValue((BigDecimal) value);
				} else if (value instanceof Integer) {
					setDoubleValue(BigDecimal.valueOf((Integer) value));
				} else if (value instanceof Long) {
					setDoubleValue(BigDecimal.valueOf((Long) value));
				} else if (value instanceof Double) {
					setDoubleValue(BigDecimal.valueOf((Double) value));
				} else if (value instanceof Float) {
					setDoubleValue(BigDecimal.valueOf(((Float) value).doubleValue()));
				} else if (value instanceof Number) {
					setDoubleValue(BigDecimal.valueOf(((Number) value).doubleValue()));
				} else {
					throw new IllegalArgumentException(MessageFormat.format("Form value [{0}] has to be [{1}], given [{2}]", 
							formAttribute.getName(), persistentType, value));
				}
				break;
			case BYTEARRAY: {
				if (value == null) {
					setByteValue(null);
				} else if (value instanceof byte[]) {
					setByteValue((byte[]) value);
				} else {
					throw new IllegalArgumentException(MessageFormat.format("Form value [{0}] has to be [{1}], given [{2}]", 
							formAttribute.getName(), persistentType, value));
				}
				break;
			}
			default:
				if (value == null) {
					setStringValue(null);
				} else if (value instanceof String) {
					setStringValue((String) value);
				} else {
					throw new IllegalArgumentException(MessageFormat.format("Form value [{0}] has to be [{1}], given [{2}]", 
							formAttribute.getName(), persistentType, value));
				}
		}
	}
	
	/**
	 * Clears all values
	 */
	public void clearValues() {
		this.booleanValue = null;
		this.stringValue = null;
		this.dateValue = null;
		this.longValue = null;
		this.doubleValue = null;
		this.byteValue = null;
	}

	/**
	 * Attribute definition
	 * 
	 * @return
	 */
	public IdmFormAttribute getFormAttribute() {
		return formAttribute;
	}

	public void setFormAttribute(IdmFormAttribute formAttribute) {
		this.formAttribute = formAttribute;
	}
	
	/**
	 * Set s owner and all attribute properties.
	 * 
	 * @param owner
	 * @param attribute
	 */
	public void setOwnerAndAttribute(O owner, IdmFormAttribute attribute) {
		setOwner(owner);
		setFormAttribute(attribute);
		if (attribute != null) {
			setPersistentType(attribute.getPersistentType());
			setConfidential(attribute.isConfidential());
		}
	}
	

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public short getSeq() {
		return seq;
	}

	public void setSeq(short seq) {
		this.seq = seq;
	}

	public void setPersistentType(PersistentType persistentType) {
		this.persistentType = persistentType;
	}

	public PersistentType getPersistentType() {
		return persistentType;
	}

	public Boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public Long getLongValue() {
		return longValue;
	}

	public void setLongValue(Long longValue) {
		this.longValue = longValue;
	}

	public BigDecimal getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(BigDecimal doubleValue) {
		this.doubleValue = doubleValue;
	}

	public DateTime getDateValue() {
		return dateValue;
	}

	public void setDateValue(DateTime dateValue) {
		this.dateValue = dateValue;
	}
	
	public byte[] getByteValue() {
		return byteValue;
	}

	public void setByteValue(byte[] byteValue) {
		this.byteValue = byteValue;
	}

	public boolean isConfidential() {
		return confidential;
	}
	
	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
	}
}
