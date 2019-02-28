package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for role system attribute mapping
 * 
 * @author svandav
 *
 */
public class SysRoleSystemAttributeFilter implements BaseFilter {

	private UUID roleSystemId;
	private Boolean isUid;
	private UUID systemMappingId;
	private String schemaAttributeName;
	private UUID systemAttributeMappingId;
	private UUID identityId;
	private UUID accountId;

	public Boolean getIsUid() {
		return isUid;
	}

	public void setIsUid(Boolean isUid) {
		this.isUid = isUid;
	}

	public UUID getRoleSystemId() {
		return roleSystemId;
	}

	public void setRoleSystemId(UUID roleSystemId) {
		this.roleSystemId = roleSystemId;
	}

	public UUID getSystemMappingId() {
		return systemMappingId;
	}

	public void setSystemMappingId(UUID systemMappingId) {
		this.systemMappingId = systemMappingId;
	}

	public String getSchemaAttributeName() {
		return schemaAttributeName;
	}

	public void setSchemaAttributeName(String schemaAttributeName) {
		this.schemaAttributeName = schemaAttributeName;
	}

	public UUID getSystemAttributeMappingId() {
		return systemAttributeMappingId;
	}

	public void setSystemAttributeMappingId(UUID systemAttributeMappingId) {
		this.systemAttributeMappingId = systemAttributeMappingId;
	}

	public UUID getIdentityId() {
		return identityId;
	}

	public void setIdentityId(UUID identityId) {
		this.identityId = identityId;
	}

	public UUID getAccountId() {
		return accountId;
	}

	public void setAccountId(UUID accountId) {
		this.accountId = accountId;
	}

}
