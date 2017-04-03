package eu.bcvsolutions.idm.core.model.dto;

import java.util.UUID;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationEvaluator;

/**
 * Assign authorization evaluator to role.
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "authorizationPolicies")
public class IdmAuthorizationPolicyDto extends AbstractDto implements AuthorizationPolicy {

	private static final long serialVersionUID = 1515971437827128049L;

	@NotNull
	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID role;
	@NotNull
	private boolean disabled;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String description;
	@Max(99999)
	private Short seq;
	private String authorizableType;
	@NotEmpty
	private String evaluatorType;
	private ConfigurationMap evaluatorProperties;
	private String basePermissions;
	
	public IdmAuthorizationPolicyDto() {
	}

	public UUID getRole() {
		return role;
	}

	public void setRole(UUID role) {
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

	public String getEvaluatorType() {
		return evaluatorType;
	}

	public void setEvaluatorType(String evaluatorType) {
		this.evaluatorType = evaluatorType;
	}
	
	@JsonIgnore
	public void setEvaluatorType(Class<? extends AuthorizationEvaluator<?>> evaluatorType) {
		Assert.notNull(evaluatorType);
		//
		this.evaluatorType = evaluatorType.getClass().getCanonicalName();
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
		return evaluatorProperties;
	}
	
	@Override
	public String getBasePermissions() {
		return basePermissions;
	}
	
	public void setBasePermissions(String basePermissions) {
		this.basePermissions = basePermissions;
	}
	
	@JsonIgnore
	public void setBasePermissions(BasePermission... permisions) {
		Assert.notNull(permisions);
		//
		this.basePermissions = StringUtils.join(permisions, ",");
	}
}
