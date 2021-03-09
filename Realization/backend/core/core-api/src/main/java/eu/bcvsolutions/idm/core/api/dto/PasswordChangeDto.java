package eu.bcvsolutions.idm.core.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringDeserializer;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dto for password change
 *
 * @author Radek Tomiška
 * @author Ondřej Kopr
 */
public class PasswordChangeDto implements Serializable {

    private static final long serialVersionUID = 8418885222359043739L;
    //
    @JsonDeserialize(using = GuardedStringDeserializer.class)
    @ApiModelProperty(notes = "Current password.", dataType = "java.lang.String", example = "admin")
    private GuardedString oldPassword;
    @NotNull
    @JsonDeserialize(using = GuardedStringDeserializer.class)
    @ApiModelProperty(required = true, notes = "New password.", dataType = "java.lang.String", example = "admin")
    private GuardedString newPassword;
    @ApiModelProperty(notes = "Change IdM password.")
    private boolean idm = false; // change in idm
    @ApiModelProperty(notes = "Change all identity's passwords on all target accounts (account's system has to support change password). IdM password is controlled by attribute 'idm' separatelly.")
    private boolean all = false; // all - all target accounts
    @ApiModelProperty(notes = "Selected AccAccounts uuids.")
    private List<String> accounts; // selected AccAccounts uuids
    @JsonIgnore
    private ZonedDateTime maxPasswordAge = null; // max password age for new password, get by password policy
    /**
	 * Skip resolving valid from date on password to check minimum days, before password can be changed again.
	 * 
	 * @since 11.0.0
	 */
    @JsonIgnore
    private boolean skipResetValidFrom = false;

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
        if (accounts == null) {
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

    public ZonedDateTime getMaxPasswordAge() {
        return maxPasswordAge;
    }

    public void setMaxPasswordAge(ZonedDateTime maxPasswordAge) {
        this.maxPasswordAge = maxPasswordAge;
    }
    
    /**
     * Skip resolving valid from date on password to check minimum days, before password can be changed again.
     * 
     * @return true - resolving valid from will be skipped
     * @since 11.0.0
     */
    public boolean isSkipResetValidFrom() {
		return skipResetValidFrom;
	}
    
    /**
     * Skip resolving valid from date on password to check minimum days, before password can be changed again.
     * 
     * @param skipResetValidFrom true - resolving valid from will be skipped
     * @since 11.0.0
     */
    public void setSkipResetValidFrom(boolean skipResetValidFrom) {
		this.skipResetValidFrom = skipResetValidFrom;
	}
}