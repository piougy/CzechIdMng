package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * Common account representation - is used for password change / reset
 * functionality - IdM account - or target system account
 * 
 * @author Radek Tomiška
 *
 */
public class IdmAccountDto implements BaseDto {

	public static final String PARAMETER_NAME = "account"; // account in parameters (eg. operation result, event paramaeters)
	//
	private static final long serialVersionUID = 1L;
	//
	private UUID id;
	private boolean idm; // IdM account
	private String uid; // username
	private String realUid; // username
	private UUID systemId; // null = IdM
	private String systemName; // null = IdM
	
	public IdmAccountDto() {
	}
	
	public IdmAccountDto(UUID id, boolean idm, String uid) {
		this.id = id;
		this.idm = idm;
		this.uid = uid;
	}
	

	public UUID getId() {
		return id;
	}

	public void setId(Serializable id) {
		this.id = EntityUtils.toUuid(id);
	}

	public boolean isIdm() {
		return idm;
	}

	public void setIdm(boolean idm) {
		this.idm = idm;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getRealUid() {
		return realUid;
	}

	public void setRealUid(String realUid) {
		this.realUid = realUid;
	}

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}
}
