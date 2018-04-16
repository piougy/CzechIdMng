package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.google.common.annotations.Beta;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * Account on target system DTO
 * 
 * @author Svanda
 *
 */

@Relation(collectionRelation = "accounts")
public class AccAccountDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	private String uid;
	private AccountType accountType;
	@Embedded(dtoClass = SysSystemDto.class)
	private UUID system;
	@Embedded(dtoClass = SysSystemEntityDto.class)
	private UUID systemEntity;
	@JsonProperty(access = Access.READ_ONLY)
	private boolean inProtection;
	private DateTime endOfProtection;
	private String realUid;
	private SystemEntityType entityType;
	@Beta
	private UUID targetEntityId;
	@Beta
	private String targetEntityType;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}

	public UUID getSystem() {
		return system;
	}

	public void setSystem(UUID system) {
		this.system = system;
	}

	public UUID getSystemEntity() {
		return systemEntity;
	}

	public void setSystemEntity(UUID systemEntity) {
		this.systemEntity = systemEntity;
	}

	public boolean isInProtection() {
		return inProtection;
	}

	public void setInProtection(boolean inProtection) {
		this.inProtection = inProtection;
	}

	public DateTime getEndOfProtection() {
		return endOfProtection;
	}

	public void setEndOfProtection(DateTime endOfProtection) {
		this.endOfProtection = endOfProtection;
	}

	/**
	 * Check if account is in protection. Validate end of protection too.
	 * 
	 * @param account
	 * @return
	 */
	public boolean isAccountProtectedAndValid() {
		if (this.isInProtection()) {
			if (this.getEndOfProtection() == null) {
				return true;
			}
			if (this.getEndOfProtection() != null && this.getEndOfProtection().isAfterNow()) {
				return true;
			}
		}
		return false;
	}

	@JsonProperty(access = Access.READ_ONLY)
	public String getRealUid() {
		return realUid;
	}

	public void setRealUid(String realUid) {
		this.realUid = realUid;
	}
	
	public SystemEntityType getEntityType() {
		return entityType;
	}
	
	public void setEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
	}

	@Beta
	public UUID getTargetEntityId() {
		return targetEntityId;
	}

	@Beta
	public void setTargetEntityId(UUID targetEntityId) {
		this.targetEntityId = targetEntityId;
	}

	@Beta
	public String getTargetEntityType() {
		return targetEntityType;
	}

	@Beta
	public void setTargetEntityType(String targetEntityType) {
		this.targetEntityType = targetEntityType;
	}
}
