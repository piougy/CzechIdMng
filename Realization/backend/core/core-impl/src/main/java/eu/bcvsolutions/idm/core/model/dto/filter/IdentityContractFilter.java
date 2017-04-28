package eu.bcvsolutions.idm.core.model.dto.filter;

import org.joda.time.LocalDate;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;

/**
 * Filter for {@link IdmIdentityContract} entities.
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityContractFilter extends QuickFilter {

	private IdmIdentity identity;
	
	private LocalDate validFrom;
	
	private LocalDate validTill;
	
	private Boolean externe;
	
	public IdmIdentity getIdentity() {
		return identity;
	}
	
	public void setIdentity(IdmIdentity identity) {
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
