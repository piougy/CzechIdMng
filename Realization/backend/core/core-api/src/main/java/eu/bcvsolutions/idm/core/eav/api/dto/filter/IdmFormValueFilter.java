package eu.bcvsolutions.idm.core.eav.api.dto.filter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Form values filter
 * 
 * TODO: data properties instead
 * 
 * @author Radek Tomiška
 * @param <O> value owner
 */
public class IdmFormValueFilter<O extends FormableEntity> extends DataFilter {

	private UUID definitionId;
	private UUID attributeId;
	private O owner;
	private PersistentType persistentType;
	private String stringValue; // equals
	private String shortTextValue; // equals
	private Boolean booleanValue; // equals
	private Long longValue; // equals
	private BigDecimal doubleValue; // equals
	private ZonedDateTime dateValue; // equals
	private UUID uuidValue; // equals

	public IdmFormValueFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmFormValueFilter(MultiValueMap<String, Object> data) {
		super(IdmFormValueDto.class, data);
	}

	public UUID getDefinitionId() {
		return definitionId;
	}

	public void setDefinitionId(UUID definitionId) {
		this.definitionId = definitionId;
	}

	public UUID getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(UUID attributeId) {
		this.attributeId = attributeId;
	}

	public O getOwner() {
		return owner;
	}

	public void setOwner(O owner) {
		this.owner = owner;
	}

	public PersistentType getPersistentType() {
		return persistentType;
	}

	public void setPersistentType(PersistentType persistentType) {
		this.persistentType = persistentType;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public String getShortTextValue() {
		return shortTextValue;
	}

	public void setShortTextValue(String shortTextValue) {
		this.shortTextValue = shortTextValue;
	}

	public Boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public Long getLongValue() {
		return longValue;
	}

	public void setLongValue(Long longValue) {
		this.longValue = longValue;
	}

	public BigDecimal getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(BigDecimal doubleValue) {
		this.doubleValue = doubleValue;
	}

	public ZonedDateTime getDateValue() {
		return dateValue;
	}

	public void setDateValue(ZonedDateTime dateValue) {
		this.dateValue = dateValue;
	}

	public UUID getUuidValue() {
		return uuidValue;
	}

	public void setUuidValue(UUID uuidValue) {
		this.uuidValue = uuidValue;
	}
}
