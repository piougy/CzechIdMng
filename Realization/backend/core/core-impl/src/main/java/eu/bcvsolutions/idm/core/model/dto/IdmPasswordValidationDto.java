package eu.bcvsolutions.idm.core.model.dto;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.security.api.domain.GuardedStringDeserializer;

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
	
	private boolean valid;

	public GuardedString getPassword() {
		return password;
	}

	public void setPassword(GuardedString password) {
		this.password = password;
	}
	
	@Override
	public String toString() {
		return this.password.asString();
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
}
