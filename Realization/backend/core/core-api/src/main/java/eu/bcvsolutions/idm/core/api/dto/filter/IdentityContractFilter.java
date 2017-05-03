package eu.bcvsolutions.idm.core.api.dto.filter;


import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import org.joda.time.LocalDate;

import java.util.UUID;

/**
 * Filter for {@link IdmIdentityDto} dtos.
 *
 * @author Radek Tomiška
 */
public class IdentityContractFilter extends QuickFilter {

    private UUID identity;

    private LocalDate validFrom;

    private LocalDate validTill;

    private Boolean externe;

    public UUID getIdentity() {
        return identity;
    }

    public void setIdentity(UUID identity) {
        this.identity = identity;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTill() {
        return validTill;
    }

    public void setValidTill(LocalDate validTill) {
        this.validTill = validTill;
    }

    public Boolean getExterne() {
        return externe;
    }

    public void setExterne(Boolean externe) {
        this.externe = externe;
    }
}
