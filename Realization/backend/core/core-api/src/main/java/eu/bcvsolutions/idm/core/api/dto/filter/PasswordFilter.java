package eu.bcvsolutions.idm.core.api.dto.filter;

import org.joda.time.LocalDate;

import java.util.UUID;

public class PasswordFilter extends QuickFilter {

    private String password;

    private LocalDate validTill;

    private LocalDate validFrom;

    private UUID identityId;

    private Boolean mustChange;

    public LocalDate getValidTill() {
        return validTill;
    }

    public void setValidTill(LocalDate validTill) {
        this.validTill = validTill;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getMustChange() {
        return mustChange;
    }

    public void setMustChange(Boolean mustChange) {
        this.mustChange = mustChange;
    }

    public UUID getIdentityId() {
        return identityId;
    }

    public void setIdentityId(UUID identityId) {
        this.identityId = identityId;
    }
}
