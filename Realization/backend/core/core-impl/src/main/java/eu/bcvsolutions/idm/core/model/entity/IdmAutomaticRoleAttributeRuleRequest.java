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
import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;

/**
 * Request defined rule for automatic role that is assignment by attribute
 * 
 * @author svandav
 * @since 8.0.0
 *
 */

@Entity
@Table(name = "idm_auto_role_att_rule_req", indexes = {
		@Index(name = "idx_idm_au_r_att_rule_id_req", columnList = "auto_role_att_id"),
		@Index(name = "idx_idm_au_r_att_rule_req_rule", columnList = "rule_id"),
		@Index(name = "idx_idm_au_r_form_att_id_req", columnList = "form_attribute_id"),
		@Index(name = "idx_idm_au_r_form_att_n_req", columnList = "attributeName"),
		@Index(name = "idx_idm_au_r_form_type_req", columnList = "type") })
public class IdmAutomaticRoleAttributeRuleRequest extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "auto_role_att_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmAutomaticRoleRequest request;

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
	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = true)
	private AutomaticRoleAttributeRuleType type = AutomaticRoleAttributeRuleType.IDENTITY;

	@Audited
	@Size(max = DefaultFieldLengths.LOG)
	private String value;

	@Audited
	@Enumerated(EnumType.STRING)
	@Column(name = "comparison", nullable = true)
	private AutomaticRoleAttributeRuleComparison comparison = AutomaticRoleAttributeRuleComparison.EQUALS;
	
	@NotNull
	@Audited
	@Column(name = "operation", nullable = false)
	@Enumerated(EnumType.STRING)
	private RequestOperationType operation;
	
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "rule_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private IdmAutomaticRoleAttributeRule rule;

	public IdmAutomaticRoleRequest getRequest() {
		return request;
	}

	public void setRequest(IdmAutomaticRoleRequest request) {
		this.request = request;
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

	public RequestOperationType getOperation() {
		return operation;
	}

	public void setOperation(RequestOperationType operation) {
		this.operation = operation;
	}

	public IdmAutomaticRoleAttributeRule getRule() {
		return rule;
	}

	public void setRule(IdmAutomaticRoleAttributeRule rule) {
		this.rule = rule;
	}

}
