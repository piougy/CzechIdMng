package eu.bcvsolutions.idm.core.model.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;

/**
 * Relation between role and definition of form-attribution. Is elementary part
 * of role form "sub-definition".
 * 
 * @author Vít Švanda
 *
 */
@Entity
@Audited
@Table(name = "idm_role_form_attribute", indexes = {
		@Index(name = "idx_idm_role_form_att_def", columnList = "attribute_id"),
		@Index(name = "idx_idm_role_form_role", columnList = "role_id"),
		@Index(name = "ux_idm_role_form_att_r_a", columnList = "attribute_id, role_id", unique = true) })
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class IdmRoleFormAttribute extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(optional = false)
	@JoinColumn(name = "attribute_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmFormAttribute formAttribute;

	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmRole role;

	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "default_value", nullable = true)
	private String defaultValue;
	
	@NotNull
	@Column(name = "required", nullable = false)
	private boolean required;
	
	@NotNull
	@Column(name = "validation_unique", nullable = false)
	private boolean unique;
	
	@Column(name = "validation_max", nullable = true, precision = 38, scale = 4)
	private BigDecimal max;
	
	@Column(name = "validation_min", nullable = true, precision = 38, scale = 4)
	private BigDecimal min;
	
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "validation_regex", nullable = true, length = DefaultFieldLengths.DESCRIPTION)
	private String regex;
	
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "validation_message", nullable = true, length = DefaultFieldLengths.DESCRIPTION)
	private String validationMessage;

	public IdmFormAttribute getFormAttribute() {
		return formAttribute;
	}

	public void setFormAttribute(IdmFormAttribute formAttribute) {
		this.formAttribute = formAttribute;
	}

	public IdmRole getRole() {
		return role;
	}

	public void setRole(IdmRole role) {
		this.role = role;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public BigDecimal getMax() {
		return max;
	}

	public void setMax(BigDecimal max) {
		this.max = max;
	}

	public BigDecimal getMin() {
		return min;
	}

	public void setMin(BigDecimal min) {
		this.min = min;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}
	
	/**
	 * Custom message, when validation fails - localization key can be used.
	 * 
	 * @return
	 * @since 9.4.0
	 */
	public String getValidationMessage() {
		return validationMessage;
	}
	
	/**
	 * Custom message, when validation fails - localization key can be used.
	 * 
	 * @param validationMessage
	 * @since 9.4.0
	 */
	public void setValidationMessage(String validationMessage) {
		this.validationMessage = validationMessage;
	}
}
