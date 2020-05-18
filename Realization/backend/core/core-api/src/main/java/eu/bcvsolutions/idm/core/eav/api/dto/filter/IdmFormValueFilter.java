package eu.bcvsolutions.idm.core.eav.api.dto.filter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Form values filter.
 * 
 * @author Radek Tomiška
 * @param <O> value owner
 */
public class IdmFormValueFilter<O extends FormableEntity> extends DataFilter {

	public static final String PARAMETER_ATTRIBUTE_ID = "attributeId"; // list - OR
	public static final String PARAMETER_DEFINITION_ID = "definitionId";
	public static final String PARAMETER_OWNER = "owner";
	public static final String PARAMETER_PERSISTENT_TYPE = "persistentType";
	public static final String PARAMETER_STRING_VALUE = "stringValue"; // equals
	public static final String PARAMETER_SHORT_TEXT_VALUE = "shortTextValue"; // equals
	public static final String PARAMETER_BOOLEAN_VALUE = "booleanValue"; // equals
	public static final String PARAMETER_LONG_VALUE = "longValue"; // equals
	public static final String PARAMETER_DOUBLE_VALUE = "doubleValue"; // equals
	public static final String PARAMETER_DATE_VALUE = "dateValue"; // equals
	public static final String PARAMETER_UUID_VALUE = "uuidValue"; // equals

	public IdmFormValueFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmFormValueFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmFormValueFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmFormValueDto.class, data, parameterConverter);
	}

	public UUID getDefinitionId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_DEFINITION_ID);
	}

	public void setDefinitionId(UUID definitionId) {
		set(PARAMETER_DEFINITION_ID, definitionId);
	}

	public UUID getAttributeId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_ATTRIBUTE_ID);
	}

	public void setAttributeId(UUID attributeId) {
		if (attributeId == null) {
    		remove(PARAMETER_ATTRIBUTE_ID);
    	} else {
    		put(PARAMETER_ATTRIBUTE_ID, Lists.newArrayList(attributeId));
    	}
	}
	
	/**
	 * Multiple attributes can be find - OR.
	 * 
	 * @return
	 * @since 10.3.0
	 */
	public List<UUID> getAttributeIds() {
		return getParameterConverter().toUuids(getData(), PARAMETER_ATTRIBUTE_ID);
	}
	
	/**
	 * Multiple attributes can be find - OR.
	 * 
	 * @param attributeIds
	 * @since 10.3.0
	 */
	public void setAttributeIds(List<UUID> attributeIds) {
		put(PARAMETER_ATTRIBUTE_ID, attributeIds);
	}

	@SuppressWarnings("unchecked")
	public O getOwner() {
		return (O) getData().getFirst(PARAMETER_OWNER);
	}

	public void setOwner(O owner) {
		set(PARAMETER_OWNER, owner);
	}

	public PersistentType getPersistentType() {
		return getParameterConverter().toEnum(getData(), PARAMETER_PERSISTENT_TYPE, PersistentType.class);
	}

	public void setPersistentType(PersistentType persistentType) {
		set(PARAMETER_PERSISTENT_TYPE, persistentType);
	}

	public String getStringValue() {
		return getParameterConverter().toString(getData(), PARAMETER_STRING_VALUE);
	}

	public void setStringValue(String stringValue) {
		set(PARAMETER_STRING_VALUE, stringValue);
	}

	public String getShortTextValue() {
		return getParameterConverter().toString(getData(), PARAMETER_SHORT_TEXT_VALUE);
	}

	public void setShortTextValue(String shortTextValue) {
		set(PARAMETER_SHORT_TEXT_VALUE, shortTextValue);
	}

	public Boolean getBooleanValue() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_BOOLEAN_VALUE);
	}

	public void setBooleanValue(Boolean booleanValue) {
		set(PARAMETER_BOOLEAN_VALUE, booleanValue);
	}

	public Long getLongValue() {
		return getParameterConverter().toLong(getData(), PARAMETER_LONG_VALUE);
	}

	public void setLongValue(Long longValue) {
		set(PARAMETER_LONG_VALUE, longValue);
	}

	public BigDecimal getDoubleValue() {
		return getParameterConverter().toBigDecimal(getData(), PARAMETER_DOUBLE_VALUE);
	}

	public void setDoubleValue(BigDecimal doubleValue) {
		set(PARAMETER_DOUBLE_VALUE, doubleValue);
	}

	public ZonedDateTime getDateValue() {
		return getParameterConverter().toDateTime(getData(), PARAMETER_DATE_VALUE);
	}

	public void setDateValue(ZonedDateTime dateValue) {
		set(PARAMETER_DATE_VALUE, dateValue);
	}

	public UUID getUuidValue() {
		return getParameterConverter().toUuid(getData(), PARAMETER_UUID_VALUE);
	}

	public void setUuidValue(UUID uuidValue) {
		set(PARAMETER_UUID_VALUE, uuidValue);
	}
}
