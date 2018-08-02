package eu.bcvsolutions.idm.core.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationEvaluator;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;
import org.springframework.util.Assert;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

/**
 * Assign authorization evaluator to role.
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "authorizationPolicies")
public class IdmAuthorizationPolicyDto extends AbstractDto implements AuthorizationPolicy, Requestable {

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
	@Size(max = DefaultFieldLengths.NAME)
	private String groupPermission;
	@Size(max = DefaultFieldLengths.NAME)
	private String authorizableType;
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String evaluatorType;
	private ConfigurationMap evaluatorProperties;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String basePermissions;
	@Embedded(dtoClass = IdmRequestItemDto.class)
	private UUID requestItem; // Isn't persist in the entity
	@Embedded(dtoClass = IdmRequestDto.class)
	private UUID request; // Isn't persist in the entity

	public IdmAuthorizationPolicyDto() {
	}

	public IdmAuthorizationPolicyDto(UUID id) {
		super(id);
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

	@Override
	public String getEvaluatorType() {
		return evaluatorType;
	}

	public void setEvaluatorType(String evaluatorType) {
		this.evaluatorType = evaluatorType;
	}

	public void setEvaluator(Class<? extends AuthorizationEvaluator<?>> evaluator) {
		Assert.notNull(evaluator);
		//
		this.evaluatorType = evaluator.getCanonicalName();
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

	@JsonIgnore
	public void setPermissions(BasePermission... permissions) {
		Assert.notNull(permissions);
		//
		this.basePermissions = StringUtils.join(permissions, ",");
	}

	@JsonIgnore
	@Override
	public Set<String> getPermissions() {
		return AuthorizationPolicy.super.getPermissions();
	}

	@Override
	public String getGroupPermission() {
		return groupPermission;
	}

	public void setGroupPermission(String groupPermission) {
		this.groupPermission = groupPermission;
	}
	
	@Override
	public UUID getRequestItem() {
		return requestItem;
	}

	@Override
	public void setRequestItem(UUID requestItem) {
		this.requestItem = requestItem;
	}

	@Override
	public UUID getRequest() {
		return request;
	}

	@Override
	public void setRequest(UUID request) {
		this.request = request;
	}
}
