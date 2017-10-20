package eu.bcvsolutions.idm.core.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringDeserializer;
import io.swagger.annotations.ApiModelProperty;

import org.joda.time.DateTime;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
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
    @ApiModelProperty(notes = "Current password.", dataType = "string", example = "admin")
    private GuardedString oldPassword;
    @NotNull
    @JsonDeserialize(using = GuardedStringDeserializer.class)
    @ApiModelProperty(required = true, notes = "New password.", dataType = "string", example = "admin")
    private GuardedString newPassword;
    @ApiModelProperty(notes = "Change IdM password.")
    private boolean idm = false; // change in idm
    @ApiModelProperty(notes = "Change all identity's passwords - IdM password and on all accounts. (account's system has to support change password)")
    private boolean all = false; // all - idm and all accounts - has higher priority
    @ApiModelProperty(notes = "Selected AccAccounts uuids.")
    private List<String> accounts; // selected AccAccounts uuids
    @JsonIgnore
    private DateTime maxPasswordAge = null; // max password age for new password, get by password policy

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

    public DateTime getMaxPasswordAge() {
        return maxPasswordAge;
    }

    public void setMaxPasswordAge(DateTime maxPasswordAge) {
        this.maxPasswordAge = maxPasswordAge;
    }
}