package eu.bcvsolutions.idm.core.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringDeserializer;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.UUID;

/**
 * Simple DTO for password validation
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */

public class IdmPasswordValidationDto implements Serializable {

    private static final long serialVersionUID = 5422443380932424940L;

    @NotNull
    @JsonDeserialize(using = GuardedStringDeserializer.class)
    private GuardedString password;

    private UUID oldPassword;

    private UUID identity;

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

    public UUID getIdentity() {
        return identity;
    }

    public void setIdentity(UUID identity) {
        this.identity = identity;
    }

    public UUID getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(UUID oldPassword) {
        this.oldPassword = oldPassword;
    }
}
