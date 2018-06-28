package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;

/**
 * Filter for {@link IdmIdentityDto} dtos.
 *
 * @author Radek Tomiška
 */
public class IdmIdentityContractFilter
		extends DataFilter
		implements CorrelationFilter, ExternalIdentifiable {

	private UUID identity;
	private UUID workPosition;
	private LocalDate validFrom;
	private LocalDate validTill;
	private Boolean externe;
	private Boolean disabled;
	private Boolean valid;
	private Boolean main;
	private Boolean validNowOrInFuture;
	private ContractState state;
	private String position;


	public IdmIdentityContractFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmIdentityContractFilter(MultiValueMap<String, Object> data) {
		super(IdmIdentityContractDto.class, data);
	}

	public IdmIdentityContractFilter(Class<? extends BaseDto> dtoClass, MultiValueMap<String, Object> data) {
		super(dtoClass, data);
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

	@Override
	public String getProperty() {
		return (String) data.getFirst(PARAMETER_CORRELATION_PROPERTY);
	}

	@Override
	public void setProperty(String property) {
		data.set(PARAMETER_CORRELATION_PROPERTY, property);
	}

	@Override
	public String getValue() {
		return (String) data.getFirst(PARAMETER_CORRELATION_VALUE);
	}

	@Override
	public void setValue(String value) {
		data.set(PARAMETER_CORRELATION_VALUE, value);
	}

	@Override
	public String getExternalId() {
		return (String) data.getFirst(PROPERTY_EXTERNAL_ID);
	}

	@Override
	public void setExternalId(String externalId) {
		data.set(PROPERTY_EXTERNAL_ID, externalId);
	}
	
	public void setPosition(String position) {
		this.position = position;
	}
	
	public String getPosition() {
		return position;
	}
	
	public void setWorkPosition(UUID workPosition) {
		this.workPosition = workPosition;
	}
	
	public UUID getWorkPosition() {
		return workPosition;
	}
}
