package eu.bcvsolutions.idm.core.api.dto.filter;

import org.joda.time.LocalDate;

import java.util.UUID;

/**
 * Password filtering
 * 
 * @author Ondřej Kopr
 * @author Radek Tomiška
 *
 */
public class IdmPasswordFilter extends QuickFilter {

    private String password;
    private LocalDate validTill;
    private LocalDate validFrom;
    private UUID identityId;
    private Boolean mustChange;
    private Boolean identityDisabled;

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
    
    public void setIdentityDisabled(Boolean identityDisabled) {
		this.identityDisabled = identityDisabled;
	}
    
    public Boolean getIdentityDisabled() {
		return identityDisabled;
	}
}
