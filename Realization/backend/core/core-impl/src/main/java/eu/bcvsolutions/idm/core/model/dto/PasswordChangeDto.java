package eu.bcvsolutions.idm.core.model.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * Dto for password change
 * 
 * @author Radek Tomiška 
 */
public class PasswordChangeDto implements Serializable {

	private static final long serialVersionUID = 8418885222359043739L;
	private String identity;
	private byte[] oldPassword;
	@NotEmpty
	private byte[] newPassword;
	private boolean idm = false; // change in idm
	private List<String> resources; // selected resources

	public String getIdentity() {
		return identity;
	}
	
	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public byte[] getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(byte[] oldPassword) {
		this.oldPassword = oldPassword;
	}

	public byte[] getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(byte[] newPassword) {
		this.newPassword = newPassword;
	}

	public boolean isIdm() {
		return idm;
	}

	public void setIdm(boolean idm) {
		this.idm = idm;
	}

	public List<String> getResources() {
		if(resources == null) {
			resources = new ArrayList<>();
		}
		return resources;
	}

	public void setResources(List<String> resources) {
		this.resources = resources;
	}
}