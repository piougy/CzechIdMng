package eu.bcvsolutions.idm.core.api.dto;

import org.joda.time.LocalDate;

public class IdmPasswordDto {

    private String password;

    private IdmIdentityDto identity;

    private LocalDate validTill;

    private LocalDate validFrom;

    private boolean mustChange = false;


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public IdmIdentityDto getIdentity() {
        return identity;
    }

    public void setIdentity(IdmIdentityDto identity) {
        this.identity = identity;
    }

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

    public boolean isMustChange() {
        return mustChange;
    }

    public void setMustChange(boolean mustChange) {
        this.mustChange = mustChange;
    }
}
