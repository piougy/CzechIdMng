package eu.bcvsolutions.idm.core.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.model.domain.IdmRoleType;

/**
 * Dto for role
 * 
 * @author svandav
 *
 */
public class IdmRoleDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	private String name;
	private boolean disabled = false;
	private Long version;
	private IdmRoleType roleType;
	private int priority = 0;
	private String approveAddWorkflow;
	private String approveRemoveWorkflow;
	private String description;
	private List<IdmRoleCompositionDto> subRoles;
	private List<IdmRoleCompositionDto> superiorRoles;
	private List<IdmRoleAuthorityDto> authorities;
	// private List<IdmRoleGuarantee> guarantees;
	// private List<IdmRoleCatalogueRole> roleCatalogues;
	public String getName() {
		return name;
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

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public IdmRoleType getRoleType() {
		return roleType;
	}

	public void setRoleType(IdmRoleType roleType) {
		this.roleType = roleType;
	}

	public int getPriority() {
		return priority;
	}

	public List<IdmRoleAuthorityDto> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<IdmRoleAuthorityDto> authorities) {
		this.authorities = authorities;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getApproveAddWorkflow() {
		return approveAddWorkflow;
	}

	public void setApproveAddWorkflow(String approveAddWorkflow) {
		this.approveAddWorkflow = approveAddWorkflow;
	}

	public String getApproveRemoveWorkflow() {
		return approveRemoveWorkflow;
	}

	public void setApproveRemoveWorkflow(String approveRemoveWorkflow) {
		this.approveRemoveWorkflow = approveRemoveWorkflow;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;

	}

	public List<IdmRoleCompositionDto> getSubRoles() {
		return subRoles;
	}

	public void setSubRoles(List<IdmRoleCompositionDto> subRoles) {
		this.subRoles = subRoles;
	}

	public List<IdmRoleCompositionDto> getSuperiorRoles() {
		return superiorRoles;
	}

	public void setSuperiorRoles(List<IdmRoleCompositionDto> superiorRoles) {
		this.superiorRoles = superiorRoles;
	}

}