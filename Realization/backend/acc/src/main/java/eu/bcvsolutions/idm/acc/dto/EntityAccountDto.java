package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

public interface EntityAccountDto extends BaseDto{

	UUID getAccount();

	void setAccount(UUID account);

	boolean isOwnership();

	void setOwnership(boolean ownership);

	UUID getEntity();

	void setEntity(UUID entity);

}