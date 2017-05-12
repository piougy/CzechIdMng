package eu.bcvsolutions.idm.core.api.dto.filter;


import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;

/**
 * Filter for {@link IdmIdentityDto} dtos.
 *
 * @author Radek Tomiška
 */
public class IdentityContractFilter extends DataFilter {

    private UUID identity;

    private LocalDate validFrom;

    private LocalDate validTill;

    private Boolean externe;
    
    public IdentityContractFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdentityContractFilter(MultiValueMap<String, Object> data) {
		super(IdmIdentityContractDto.class, data);
	}

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
