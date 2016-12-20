package eu.bcvsolutions.idm.core.model.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.security.api.domain.GuardedStringAsByteDeserializer;

/**
 * Dto for password change
 * 
 * @author Radek Tomiška 
 */
public class PasswordChangeDto implements Serializable {

	private static final long serialVersionUID = 8418885222359043739L;
	private String identity;
	@JsonDeserialize(using = GuardedStringAsByteDeserializer.class)
	private GuardedString oldPassword;
	@NotNull
	@JsonDeserialize(using = GuardedStringAsByteDeserializer.class)
	private GuardedString newPassword;
	private boolean idm = false; // change in idm
	private boolean all = false; // all - idm and all accounts - has higher priority
	private List<String> accounts; // selected accounts

	public String getIdentity() {
		return identity;
	}
	
	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public GuardedString getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(GuardedString oldPassword) {
		this.oldPassword = oldPassword;
	}

	public GuardedString getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(GuardedString newPassword) {
		this.newPassword = newPassword;
	}

	public boolean isIdm() {
		return idm;
	}

	public void setIdm(boolean idm) {
		this.idm = idm;
	}

	public List<String> getAccounts() {
		if(accounts == null) {
			accounts = new ArrayList<>();
		}
		return accounts;
	}

	public void setAccounts(List<String> accounts) {
		this.accounts = accounts;
	}
	
	public void setAll(boolean all) {
		this.all = all;
	}
	
	public boolean isAll() {
		return all;
	}
}