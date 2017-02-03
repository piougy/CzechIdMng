package eu.bcvsolutions.idm.core.model.dto;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringDeserializer;

/**
 * Simple DTO for password validation
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IdmPasswordValidationDto implements Serializable {

	private static final long serialVersionUID = 5422443380932424940L;
	
	@NotNull
	@JsonDeserialize(using = GuardedStringDeserializer.class)
	private GuardedString password;
	
	private IdmPassword oldPassword;
	
	private IdmIdentity identity;
	
	@JsonIgnore
	private boolean valid;

	public GuardedString getPassword() {
		return password;
	}

	public void setPassword(GuardedString password) {
		this.password = password;
	}
	
	public void setPassword(String password) {
		this.password = new GuardedString(password);
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public IdmIdentity getIdentity() {
		return identity;
	}

	public void setIdentity(IdmIdentity identity) {
		this.identity = identity;
	}

	public IdmPassword getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(IdmPassword oldPassword) {
		this.oldPassword = oldPassword;
	}
}
