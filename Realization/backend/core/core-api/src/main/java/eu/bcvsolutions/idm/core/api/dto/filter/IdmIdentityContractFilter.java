package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;

/**
 * Filter for {@link IdmIdentityDto} dtos.
 *
 * @author Radek Tomiška
 */
public class IdmIdentityContractFilter extends DataFilter {

	private UUID identity;
	private LocalDate validFrom;
	private LocalDate validTill;
	private Boolean externe;
	private Boolean disabled;
	private Boolean valid;
	private Boolean main;
	private Boolean validNowOrInFuture;
	private ContractState state;

	public IdmIdentityContractFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmIdentityContractFilter(MultiValueMap<String, Object> data) {
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

	public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public Boolean getMain() {
		return main;
	}

	public void setMain(Boolean main) {
		this.main = main;
	}
	
	public void setValidNowOrInFuture(Boolean validNowOrInFuture) {
		this.validNowOrInFuture = validNowOrInFuture;
	}
	
	public Boolean getValidNowOrInFuture() {
		return validNowOrInFuture;
	}
	
	public void setState(ContractState state) {
		this.state = state;
	}
	
	public ContractState getState() {
		return state;
	}
}
