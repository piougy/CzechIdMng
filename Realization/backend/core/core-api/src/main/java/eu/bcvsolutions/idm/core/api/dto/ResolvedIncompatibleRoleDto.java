package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import io.swagger.annotations.ApiModelProperty;

/**
 * Segregation of Duties - resolved incompatible role for the given owner role (used in concept, business role, directly assigned role)
 * 
 * @author Radek Tomiška 
 * @since 9.4.0
 */
@Relation(collectionRelation = "incompatibleRoles")
public class ResolvedIncompatibleRoleDto implements BaseDto {

    private static final long serialVersionUID = 1L;
    
    @NotNull
    @JsonDeserialize(as = UUID.class)
	@ApiModelProperty(required = true, notes = "Unique uuid identifier. Used as incompatible role identifier in rest endpoints", dataType = "java.util.UUID")
	private UUID id;
    @NotNull
    @ApiModelProperty(notes = "Role, which cause incompatibility - owner role used in concept, business role, directly assigned role")
    private IdmRoleDto directRole;
    @NotNull
    @ApiModelProperty(notes = "Role, which cause incompatibility - owner role used in concept, business role, directly assigned role")
    private IdmIncompatibleRoleDto incompatibleRole;
    
    public ResolvedIncompatibleRoleDto() {
	}
    
    public ResolvedIncompatibleRoleDto(IdmRoleDto directRole, IdmIncompatibleRoleDto incompatibleRole) {
    	this.directRole = directRole;
    	this.incompatibleRole = incompatibleRole;
		this.id = incompatibleRole == null ? null : incompatibleRole.getId();
	}
    
    @Override
	public UUID getId() {
		return id;
	}

	@Override
	public void setId(Serializable id) {
		this.id = EntityUtils.toUuid(id);
	}

	public IdmRoleDto getDirectRole() {
		return directRole;
	}

	public void setDirectRole(IdmRoleDto directRole) {
		this.directRole = directRole;
	}

	public IdmIncompatibleRoleDto getIncompatibleRole() {
		return incompatibleRole;
	}

	public void setIncompatibleRole(IdmIncompatibleRoleDto incompatibleRole) {
		this.incompatibleRole = incompatibleRole;
		this.id = incompatibleRole == null ? null : incompatibleRole.getId();
	}
	
	public boolean equals(final Object o) {
		if (!(o instanceof ResolvedIncompatibleRoleDto)) {
			return false;
		}
		ResolvedIncompatibleRoleDto that = (ResolvedIncompatibleRoleDto) o;
		return new EqualsBuilder()
				.append(directRole, that.directRole)
				.append(incompatibleRole, that.incompatibleRole)
				.isEquals();
	}

	public int hashCode() {
		 return new HashCodeBuilder()
				 .append(directRole)
				 .append(incompatibleRole)
				 .toHashCode();
	}
}