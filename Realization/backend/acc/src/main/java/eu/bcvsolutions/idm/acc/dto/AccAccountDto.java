package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * Account on target system DTO
 * 
 * @author Svanda
 *
 */
public class AccAccountDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	private String uid;
	private AccountType accountType;
	@Embedded(dtoClass = SysSystemDto.class)
	private UUID system;
	private SysSystemEntity systemEntity;

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

	public SysSystemEntity getSystemEntity() {
		return systemEntity;
	}

	public void setSystemEntity(SysSystemEntity systemEntity) {
		this.systemEntity = systemEntity;
	}

}
