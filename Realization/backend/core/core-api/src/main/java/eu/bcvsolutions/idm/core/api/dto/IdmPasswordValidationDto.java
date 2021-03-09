package eu.bcvsolutions.idm.core.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringDeserializer;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for password validation.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */
public class IdmPasswordValidationDto implements Serializable {

    private static final long serialVersionUID = 5422443380932424940L;
    //
    @NotNull
    @JsonDeserialize(using = GuardedStringDeserializer.class)
    private GuardedString password; // new password
    private UUID oldPassword; // old password identifier
    private IdmIdentityDto identity; // password owner
    /**
     * Validation to check minimum days, before password can be changed again will be enforced, 
     * even when password is changed under different identity (e.g. from password filter).
     * 
     * @since 11.0.0
     */
    @JsonIgnore
	private boolean enforceMinPasswordAgeValidation = false;
    

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

    public IdmIdentityDto getIdentity() {
        return identity;
    }

    public void setIdentity(IdmIdentityDto identity) {
        this.identity = identity;
    }

    public UUID getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(UUID oldPassword) {
        this.oldPassword = oldPassword;
    }
    
    /**
     * Validation to check minimum days, before password can be changed again will be enforced, 
     * even when password is changed under different identity (e.g. from password filter).
     * 
     * @return true => validated
     * @since 11.0.0
     */
    public boolean isEnforceMinPasswordAgeValidation() {
		return enforceMinPasswordAgeValidation;
	}
    
    /**
     * Validation to check minimum days, before password can be changed again will be enforced, 
     * even when password is changed under different identity (e.g. from password filter).
     * 
     * @param enforceMinPasswordAgeValidation true => validated
     * @since 11.0.0
     */
    public void setEnforceMinPasswordAgeValidation(boolean enforceMinPasswordAgeValidation) {
		this.enforceMinPasswordAgeValidation = enforceMinPasswordAgeValidation;
	}
}
