package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonBackReference;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;

/**
 * Assign authorization evaluator to role.
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Audited
@Table(name = "idm_authorization_policy", indexes = {
		@Index(name = "idx_idm_author_policy_role", columnList = "role_id"),
		@Index(name = "idx_idm_author_policy_a_t", columnList = "authorizable_type")
})
public class IdmAuthorizationPolicy extends AbstractEntity implements AuthorizationPolicy {
	
	private static final long serialVersionUID = -5925961560301302926L;

	@NotNull
	@JsonBackReference
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )	
	private IdmRole role;
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "authorizable_type", length = DefaultFieldLengths.NAME)
	private String authorizableType;
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "group_permission", length = DefaultFieldLengths.NAME)
	private String groupPermission;
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "base_permissions", length = DefaultFieldLengths.NAME)
	//@Convert(converter = StringSetConverter.class) // TODO: doesn't work with envers
	private String basePermissions;
	
	@NotEmpty
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "evaluator_type", length = DefaultFieldLengths.NAME, nullable = false)
	private String evaluatorType;
	
	@Column(name = "evaluator_properties", length = Integer.MAX_VALUE)
	private ConfigurationMap evaluatorProperties;
	
	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled;
	
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", length = DefaultFieldLengths.DESCRIPTION)
	private String description;
	
	@Max(99999)
	@Column(name = "seq")
	private Short seq;
	
	
	public IdmAuthorizationPolicy() {
	}
	
	public IdmAuthorizationPolicy(UUID id) {
		super(id);
	}
	
	public IdmRole getRole() {
		return role;
	}

	public void setRole(IdmRole role) {
		this.role = role;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Short getSeq() {
		return seq;
	}

	public void setSeq(Short seq) {
		this.seq = seq;
	}

	@Override
	public String getEvaluatorType() {
		return evaluatorType;
	}

	public void setEvaluatorType(String evaluatorType) {
		this.evaluatorType = evaluatorType;
	}
	
	public void setAuthorizableType(String authorizableType) {
		this.authorizableType = authorizableType;
	}
	
	@Override
	public String getAuthorizableType() {
		return authorizableType;
	}

	public void setEvaluatorProperties(ConfigurationMap evaluatorProperties) {
		this.evaluatorProperties = evaluatorProperties;
	}
	
	@Override
	public ConfigurationMap getEvaluatorProperties() {
		if (evaluatorProperties == null) {
			evaluatorProperties = new ConfigurationMap();
		}
		return evaluatorProperties;
	}
	
	@Override
	public String getBasePermissions() {
		return basePermissions;
	}
	
	public void setBasePermissions(String basePermissions) {
		this.basePermissions = basePermissions;
	}
	
	public void setGroupPermission(String groupPermission) {
		this.groupPermission = groupPermission;
	}
	
	@Override
	public String getGroupPermission() {
		return groupPermission;
	}
}
