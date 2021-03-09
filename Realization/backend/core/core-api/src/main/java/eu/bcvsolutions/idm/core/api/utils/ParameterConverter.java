package eu.bcvsolutions.idm.core.api.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.LookupService;

/**
 * Rest controller helpers
 * - parameters converters
 * 
 * TODO: toDto, toDtoId
 * 
 * @author Radek Tomiška
 */
public class ParameterConverter {

	private LookupService lookupService;
	
	public ParameterConverter() {
	}
	
	public ParameterConverter(LookupService lookupService) {
		this.lookupService = lookupService;
	}
	
	public void setLookupService(LookupService lookupService) {
		this.lookupService = lookupService;
	}
	
	/**
	 * Reads {@code String} parameter from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public String toString(MultiValueMap<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters, "Input parameters are required.");
	    //
		return toString(toSingleValueMap(parameters), parameterName);
	}
	
	/**
	 * Reads {@code String} parameter from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public String toString(Map<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters, "Input parameters are required.");
	    Assert.notNull(parameterName, "Parameter name is required.");
	    //
		return toString(parameters.get(parameterName));
	}
	
	/**
	 * Converts given value to string ("naive" toString)
	 * 
	 * @param parameterValue
	 * @return
	 */
	public String toString(Object parameterValue) {
		if (parameterValue == null) {
			return null;
		}
		if (parameterValue instanceof String) {
			return (String) parameterValue;
		}
		return parameterValue.toString();
	}
	
	/**
	 * Converts parameter to list of {@code String} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public List<String> toStrings(MultiValueMap<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters, "Input parameters are required.");
		//
		List<String> results = new ArrayList<>();
		//
		List<Object> strings = parameters.get(parameterName);
		if (strings == null) {
			return results;
		}
		//
		strings.forEach(value -> {
			results.add(toString(value));
		});
		//
		return results;
	}
	
	/**
	 * Converts parameter to {@code Boolean} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public Boolean toBoolean(MultiValueMap<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters, "Input parameters are required.");
		//
		return toBoolean(toSingleValueMap(parameters), parameterName);
	}
	
	/**
	 * Converts parameter to {@code boolean} from given parameters, or returns default value, if no value is given
	 * 
	 * @param parameters
	 * @param parameterName
	 * @param deafultValue
	 * @return
	 */
	public boolean toBoolean(MultiValueMap<String, Object> parameters, String parameterName, boolean deafultValue) {
		Assert.notNull(parameters, "Input parameters are required.");
	    //
		return toBoolean(toSingleValueMap(parameters), parameterName, deafultValue);
	}
	
	/**
	 * Converts parameter to {@code boolean} from given parameters, or returns default value, if no value is given
	 * 
	 * @param parameters
	 * @param parameterName
	 * @param defaultValue
	 * @return
	 */
	public boolean toBoolean(Map<String, Object> parameters, String parameterName, boolean defaultValue) {
		Assert.notNull(parameters, "Input parameters are required.");
		//
		Boolean result = toBoolean(parameters, parameterName);
		return result == null ? defaultValue : result;
	}
	
	/**
	 * Converts parameter to {@code Boolean} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public Boolean toBoolean(Map<String, Object> parameters, String parameterName) {
		return toBoolean(toString(parameters, parameterName));
	}
	
	/**
	 * Converts parameter to {@code Boolean} from given parameters.
	 *  
	 * @param parameterValue
	 * @return
	 */
	public Boolean toBoolean(String parameterValue) {
		if (StringUtils.isNotEmpty(parameterValue)) {
			return Boolean.valueOf(parameterValue);
		}
		return null;
	}
	
	/**
	 * Converts parameter to {@code Integer} from given parameters.
	 * 
	 * @param parameters data
	 * @param parameterName parameter name
	 * @return value
	 * @since 11.0.0
	 */
	public Integer toInteger(MultiValueMap<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters, "Input parameters are required.");
		//
		return toInteger(toSingleValueMap(parameters), parameterName);
	}
	
	/**
	 * Converts parameter to {@code Integer} from given parameters.
	 * 
	 * @param parameters data
	 * @param parameterName parameter name
	 * @return value
	 * @since 11.0.0
	 */
	public Integer toInteger(Map<String, Object> parameters, String parameterName) {
		String valueAsString = toString(parameters, parameterName);
		if (StringUtils.isNotEmpty(valueAsString)) {
			try {
				return Integer.valueOf(valueAsString);
			} catch (NumberFormatException ex) {
				throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of(parameterName, valueAsString), ex);
			}		
		}
		return null;
	}
	
	/**
	 * Converts parameter to {@code Integer} from given parameters.
	 * 
	 * @param parameters data
	 * @param parameterName parameter or property name
	 * @param defaultValue default value
	 * @return parameter value or default value, if parameters value is not set
	 * @since 11.0.0
	 */
	public int toInteger(Map<String, Object> parameters, String parameterName, int defaultValue) {
		Integer result = toInteger(parameters, parameterName);
		//
		return result == null ? defaultValue : result;
	}
	
	/**
	 * Converts parameter to {@code Long} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public Long toLong(MultiValueMap<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters, "Input parameters are required.");
		//
		return toLong(toSingleValueMap(parameters), parameterName);
	}
	
	/**
	 * Converts parameter to {@code Long} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public Long toLong(Map<String, Object> parameters, String parameterName) {
		String valueAsString = toString(parameters, parameterName);
		if (StringUtils.isNotEmpty(valueAsString)) {
			try {
				return Long.valueOf(valueAsString);
			} catch (NumberFormatException ex) {
				throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of(parameterName, valueAsString), ex);
			}		
		}
		return null;
	}
	
	/**
	 * Converts parameter to {@code Long} from given parameters.
	 * 
	 * @param parameters map
	 * @param parameterName parameter or property name
	 * @param defaultValue default value
	 * @return parameter value or default value, if parameters value is not set
	 * @since 10.6.0
	 */
	public long toLong(Map<String, Object> parameters, String parameterName, long defaultValue) {
		Long result = toLong(parameters, parameterName);
		//
		return result == null ? defaultValue : result;
	}
	
	/**
	 * Converts parameter to {@code BigDecimal} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 * @since 10.3.0
	 */
	public BigDecimal toBigDecimal(MultiValueMap<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters, "Input parameters are required.");
		//
		return toBigDecimal(toSingleValueMap(parameters), parameterName);
	}
	
	/**
	 * Converts parameter to {@code Double} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 * @since 10.3.0
	 */
	public BigDecimal toBigDecimal(Map<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters, "Input parameters are required.");
	    Assert.notNull(parameterName, "Parameter name is required.");
	    //
	    Object value = parameters.get(parameterName);
	    if (value == null) {
			return null;
		}
	    if (value instanceof BigDecimal) {
			return (BigDecimal) value;
		}
		//
		String valueAsString = toString(value);
		if (StringUtils.isNotEmpty(valueAsString)) {
			try {
				return new BigDecimal((String) valueAsString);
			} catch (NumberFormatException ex) {
				throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of(parameterName, valueAsString), ex);
			}		
		}
		return null;
	}
	
	/**
	 * Converts parameter to {@code UUID} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public UUID toUuid(MultiValueMap<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters, "Input parameters are required.");
	    //
		return toUuid(toSingleValueMap(parameters), parameterName);
	}
	
	/**
	 * Converts parameter to list of {@code UUID} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public List<UUID> toUuids(MultiValueMap<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters, "Input parameters are required.");
		//
		List<UUID> results = new ArrayList<>();
		//
		List<Object> uuids = parameters.get(parameterName);
		if (uuids == null) {
			return results;
		}
		//
		uuids.forEach(uuid -> {
			results.add(EntityUtils.toUuid(uuid));
		});
		
		return results;
	}
	
	/**
	 * Converts parameter to list of {@code UUID} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public List<UUID> toUuids(Map<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters, "Input parameters are required.");
		//
		List<UUID> results = new ArrayList<>();
		//
		Object asObject = parameters.get(parameterName);
		if (asObject == null) {
			return results;
		}
		if (!(asObject instanceof Collection)) {
			results.add(DtoUtils.toUuid(asObject));
		} else {
			((Collection<?>) asObject).forEach(uuid -> {
				if (uuid != null) {
					results.add(DtoUtils.toUuid(uuid));
				}
			});
		}
		//
		return results;
	}
	
	/**
	 * Converts parameter to {@code UUID} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public UUID toUuid(Map<String, Object> parameters, String parameterName) {
		// supports UUID and String representation
		return DtoUtils.toUuid(parameters.get(parameterName));
	}
	
	/**
	 * Converts parameter to given {@code enumClass} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @param enumClass
	 * @return
	 */
	public <T extends Enum<T>> T toEnum(MultiValueMap<String, Object> parameters, String parameterName, Class<T> enumClass) {
		Assert.notNull(parameters, "Input parameters are required.");
	    //
		return toEnum(toSingleValueMap(parameters), parameterName, enumClass);
	}
	
	/**
	 * Converts parameter to given list of {@code enumClass} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @param enumClass
	 * @return
	 */
	public <T extends Enum<T>> List<T> toEnums(MultiValueMap<String, Object> parameters, String parameterName, Class<T> enumClass) {
		Assert.notNull(parameters, "Input parameters are required.");
		//
		List<T> results = new ArrayList<>();
		List<Object> parameterValues = parameters.get(parameterName);
		if (parameterValues == null) {
			return results;
		}
		//
		parameterValues.forEach(parameterValue -> {
			results.add(toEnum(toString(parameterValue), parameterName, enumClass));
		});
		//
		return results;
	}
	
	/**
	 * Converts parameter to given {@code enumClass} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @param enumClass
	 * @return
	 */
	public <T extends Enum<T>> T toEnum(Map<String, Object> parameters, String parameterName, Class<T> enumClass) {
		Assert.notNull(enumClass, "Enum class name is required.");
	    //
	    return toEnum(toString(parameters, parameterName), parameterName, enumClass);
	}
	
	/**
	 * Converts parameter to given {@code enumClass} from given parameter value.
	 * 
	 * @param parameterValue
	 * @param parameterName
	 * @param enumClass
	 * @return
	 */
	public <T extends Enum<T>> T toEnum(String parameterValue, String parameterName, Class<T> enumClass) {
		Assert.notNull(enumClass, "Enum class name is required.");
	    //
	    if(StringUtils.isEmpty(parameterValue)) {
	    	return null;
	    }
        try {
            return Enum.valueOf(enumClass, parameterValue.trim().toUpperCase());
        } catch(IllegalArgumentException ex) {
        	throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of(parameterName, parameterValue), ex);
        }
	}
	
	/**
	 * Converts parameter to given {@code identifiableType} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @param identifiableType
	 * @return
	 */
	public <T extends BaseEntity> T toEntity(MultiValueMap<String, Object> parameters, String parameterName, Class<T> identifiableType) {
		Assert.notNull(parameters, "Input parameters are required.");
	    //
		return toEntity(toSingleValueMap(parameters), parameterName, identifiableType);
	}
	
	/**
	 * Converts parameter to given {@code identifiableType} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @param identifiableType
	 * @return
	 */
	public <T extends BaseEntity> T toEntity(Map<String, Object> parameters, String parameterName, Class<T> identifiableType) {
		return toEntity(toString(parameters, parameterName), identifiableType);
	}
	
	/**
	 * Converts parameter to given {@code identifiableType} from given parameters.
	 * 
	 * @param parameterValue
	 * @param parameterName
	 * @param identifiableType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Identifiable> T toEntity(String parameterValue, Class<T> identifiableType) {
	    if(StringUtils.isEmpty(parameterValue)) {
	    	return null;
	    }
	    Assert.notNull(lookupService, "Lookup service is not defined. Initialize converter properly.");
		T entity = (T) lookupService.lookupEntity(identifiableType, parameterValue);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE, "Entity type [%s] with identifier [%s] does not found", ImmutableMap.of("identifiableType", identifiableType.getSimpleName(), "identifier", parameterValue));
		}
		return entity;
	}
	
	/**
	 * Converts parameter to entity id.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @param identifiableType dto or entity type
	 * @return
	 */
	public UUID toEntityUuid(MultiValueMap<String, Object> parameters, String parameterName, Class<? extends Identifiable> identifiableType) {
		Assert.notNull(parameters, "Input parameters are required.");
		//
		return toEntityUuid(toSingleValueMap(parameters), parameterName, identifiableType);
	}
	
	/**
	 * Converts parameter to entity id.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @param identifiableType
	 * @return
	 * 
	 * @since 9.6.3
	 */
	public UUID toEntityUuid(Map<String, Object> parameters, String parameterName, Class<? extends Identifiable> identifiableType) {
		return toEntityUuid(toString(parameters, parameterName), identifiableType);
	}
	
	/**
	 * Converts parameter value to entity id.
	 * 
	 * @param parameterValue
	 * @param identifiableType dto or entity type
	 * @return
	 */
	public UUID toEntityUuid(String parameterValue, Class<? extends Identifiable> identifiableType) {
		Identifiable entity = toEntity(parameterValue, identifiableType);
		return entity == null ? null : (UUID) entity.getId();
	}
	
	/**
	 * Converts parameter {@code DateTime} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public ZonedDateTime toDateTime(MultiValueMap<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters, "Input parameters are required.");
	    //
		return toDateTime(toSingleValueMap(parameters), parameterName);
	}
	
	/**
	 * Converts parameter {@code DateTime} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public ZonedDateTime toDateTime(Map<String, Object> parameters, String parameterName) {
		Object valueAsObject = parameters.get(parameterName);
		if (valueAsObject instanceof ZonedDateTime) {
			return (ZonedDateTime) valueAsObject;
		}
		String valueAsString = toString(parameters, parameterName);
		if (valueAsString == null || valueAsString.isEmpty()) {
			return null;
		} else {
			return ZonedDateTime.parse(valueAsString);
		}
	}
	
	/**
	 * Converts parameter {@code DateTime} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public LocalDate toLocalDate(MultiValueMap<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters, "Input parameters are required.");
	    //
		return toLocalDate(toSingleValueMap(parameters), parameterName);
	}
	
	/**
	 * Converts parameter {@code DateTime} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public LocalDate toLocalDate(Map<String, Object> parameters, String parameterName) {
		String valueAsString = toString(parameters, parameterName);
		if (valueAsString == null || valueAsString.isEmpty()) {
			return null;
		} else {
			return LocalDate.parse(valueAsString);
		}
	}
	
	/**
	 * Transform to single value map.
	 * 
	 * @param parameters
	 * @return
	 */
	protected Map<String, Object> toSingleValueMap(MultiValueMap<String, Object> parameters) {
		LinkedHashMap<String, Object> singleValueMap = new LinkedHashMap<String, Object>();
		for (Entry<String, List<Object>> entry : parameters.entrySet()) {
			if (CollectionUtils.isNotEmpty(entry.getValue())) {
				singleValueMap.put(entry.getKey(), entry.getValue().get(0));
			}
		}
		return singleValueMap;
	}
}
