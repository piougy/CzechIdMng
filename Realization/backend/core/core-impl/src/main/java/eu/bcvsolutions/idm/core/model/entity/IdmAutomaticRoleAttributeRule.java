package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;

/**
 * Rule definition for automatic role that is assignment by attribute
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @since 7.6.0
 *
 */

@Entity
@Table(name = "idm_auto_role_att_rule", indexes = {
		@Index(name = "idx_idm_auto_role_att_rule_id", columnList = "auto_role_att_id"),
		@Index(name = "idx_idm_auto_role_form_att_id", columnList = "form_attribute_id"),
		@Index(name = "idx_idm_auto_role_form_att_name", columnList = "attributeName"),
		@Index(name = "idx_idm_auto_role_form_type", columnList = "type") })
public class IdmAutomaticRoleAttributeRule extends AbstractEntity {

	private static final long serialVersionUID = -6497080117556441775L;

	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "auto_role_att_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmAutomaticRoleAttribute automaticRoleAttribute;

	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "form_attribute_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmFormAttribute formAttribute;

	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "attributeName", nullable = true, length = DefaultFieldLengths.NAME)
	private String attributeName;

	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private AutomaticRoleAttributeRuleType type = AutomaticRoleAttributeRuleType.IDENTITY;

	@Audited
	@Size(max = DefaultFieldLengths.LOG)
	private String value;

	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "comparison", nullable = false)
	private AutomaticRoleAttributeRuleComparison comparison = AutomaticRoleAttributeRuleComparison.EQUALS;

	public IdmAutomaticRoleAttribute getAutomaticRoleAttribute() {
		return automaticRoleAttribute;
	}

	public void setAutomaticRoleAttribute(IdmAutomaticRoleAttribute automaticRoleAttribute) {
		this.automaticRoleAttribute = automaticRoleAttribute;
	}

	public IdmFormAttribute getFormAttribute() {
		return formAttribute;
	}

	public void setFormAttribute(IdmFormAttribute formAttribute) {
		this.formAttribute = formAttribute;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public AutomaticRoleAttributeRuleType getType() {
		return type;
	}

	public void setType(AutomaticRoleAttributeRuleType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public AutomaticRoleAttributeRuleComparison getComparison() {
		return comparison;
	}

	public void setComparison(AutomaticRoleAttributeRuleComparison comparison) {
		this.comparison = comparison;
	}

}
