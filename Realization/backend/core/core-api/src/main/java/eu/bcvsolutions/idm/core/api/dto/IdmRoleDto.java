package eu.bcvsolutions.idm.core.api.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.domain.RoleType;
import io.swagger.annotations.ApiModelProperty;

/**
 * Dto for role
 *
 * @author svandav
 * @author Radek Tomiška
 */
@Relation(collectionRelation = "roles")
public class IdmRoleDto extends AbstractDto implements Disableable, Codeable, ExternalIdentifiable {

    private static final long serialVersionUID = 1L;

    @NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
    private String name;
    @Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(notes = "Unique external identifier.")
	private String externalId;
    private boolean disabled;
    private boolean canBeRequested;
    private RoleType roleType = RoleType.TECHNICAL;
    private int priority = 0;
    private boolean approveRemove;
    @Size(max = DefaultFieldLengths.DESCRIPTION)
    private String description;
    //
    @Deprecated // @since 8.2.0 - will be removed in 9 - business role redesign
    private List<IdmRoleCompositionDto> subRoles;
    @Deprecated // @since 8.2.0 - will be removed in 9 - business role redesign
    @JsonProperty(access = Access.READ_ONLY)
    private List<IdmRoleCompositionDto> superiorRoles;
    @Deprecated // @since 8.2.0 - use solo endpoint
    private List<IdmRoleGuaranteeDto> guarantees;
    @Deprecated // @since 8.2.0 - use solo endpoint
    private List<IdmRoleCatalogueRoleDto> roleCatalogues;

    public IdmRoleDto() {
	}
    
    public IdmRoleDto(UUID id) {
    	super(id);
	}
    
    public String getName() {
        return name;
    }
    
    @Override
    public String getCode() {
    	return getName();
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;

    }

    public List<IdmRoleCompositionDto> getSubRoles() {
    	if (subRoles == null) {
    		subRoles = new ArrayList<>();
    	}
        return subRoles;
    }

    public void setSubRoles(List<IdmRoleCompositionDto> subRoles) {
        this.subRoles = subRoles;
    }

    public List<IdmRoleCompositionDto> getSuperiorRoles() {
    	if (superiorRoles == null) {
    		superiorRoles = new ArrayList<>();
    	}
        return superiorRoles;
    }

    public void setSuperiorRoles(List<IdmRoleCompositionDto> superiorRoles) {
        this.superiorRoles = superiorRoles;
    }

    public boolean isApproveRemove() {
        return approveRemove;
    }

    public void setApproveRemove(boolean approveRemove) {
        this.approveRemove = approveRemove;
    }
    
    public boolean isCanBeRequested() {
		return canBeRequested;
	}
    
    public void setCanBeRequested(boolean canBeRequested) {
		this.canBeRequested = canBeRequested;
	}

	public List<IdmRoleGuaranteeDto> getGuarantees() {
		if (guarantees == null) {
			guarantees = new ArrayList<>();
		}
		return guarantees;
	}

	public void setGuarantees(List<IdmRoleGuaranteeDto> guarantees) {
		this.guarantees = guarantees;
	}

	public List<IdmRoleCatalogueRoleDto> getRoleCatalogues() {
		if (roleCatalogues == null) {
			roleCatalogues = new ArrayList<>();
		}
		return roleCatalogues;
	}

	public void setRoleCatalogues(List<IdmRoleCatalogueRoleDto> roleCatalogues) {
		this.roleCatalogues = roleCatalogues;
	}
	
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}
}