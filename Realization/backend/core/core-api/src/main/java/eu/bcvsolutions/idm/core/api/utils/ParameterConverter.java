package eu.bcvsolutions.idm.core.api.utils;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.LookupService;

/**
 * Rest controller helpers
 * - parameters converters
 * 
 * @author Radek Tomiška
 */
public class ParameterConverter {

	private final LookupService lookupService;
	
	public ParameterConverter(LookupService lookupService) {
		Assert.notNull(lookupService);
		//
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
		Assert.notNull(parameters);
	    //
		return toString(parameters.toSingleValueMap(), parameterName);
	}
	
	/**
	 * Reads {@code String} parameter from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public String toString(Map<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters);
	    Assert.notNull(parameterName);
	    //
		return (String)parameters.get(parameterName);
	}
	
	/**
	 * Converts parameter to {@code Boolean} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public Boolean toBoolean(MultiValueMap<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters);
		//
		return toBoolean(parameters.toSingleValueMap(), parameterName);
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
		Assert.notNull(parameters);
		//
		Boolean result = toBoolean(parameters, parameterName);
		return result == null ? deafultValue : result;
	}
	
	/**
	 * Converts parameter to {@code Boolean} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public Boolean toBoolean(Map<String, Object> parameters, String parameterName) {
		String valueAsString = toString(parameters, parameterName);
		if (StringUtils.isNotEmpty(valueAsString)) {
			return Boolean.valueOf(valueAsString);
		}
		return null;
	}
	
	/**
	 * Converts parameter to {@code Long} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public Long toLong(MultiValueMap<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters);
		//
		return toLong(parameters.toSingleValueMap(), parameterName);
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
		if(StringUtils.isNotEmpty(valueAsString)) {
			try {
				return Long.valueOf(valueAsString);
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
		Assert.notNull(parameters);
	    //
		return toUuid(parameters.toSingleValueMap(), parameterName);
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
		return EntityUtils.toUuid(parameters.get(parameterName));
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
		Assert.notNull(parameters);
	    //
		return toEnum(parameters.toSingleValueMap(), parameterName, enumClass);
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
		Assert.notNull(enumClass);
	    //
	    String valueAsString = toString(parameters, parameterName);
	    if(StringUtils.isEmpty(valueAsString)) {
	    	return null;
	    }
        try {
            return Enum.valueOf(enumClass, valueAsString.trim().toUpperCase());
        } catch(IllegalArgumentException ex) {
        	throw new ResultCodeException(CoreResultCode.BAD_VALUE, ImmutableMap.of(parameterName, valueAsString), ex);
        }
	}
	
	/**
	 * Converts parameter to given {@code entityClass} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @param entityClass
	 * @return
	 */
	public <T extends BaseEntity> T toEntity(MultiValueMap<String, Object> parameters, String parameterName, Class<T> entityClass) {
		Assert.notNull(parameters);
	    //
		return toEntity(parameters.toSingleValueMap(), parameterName, entityClass);
	}
	
	/**
	 * Converts parameter to given {@code entityClass} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @param entityClass
	 * @return
	 */
	public <T extends BaseEntity> T toEntity(Map<String, Object> parameters, String parameterName, Class<T> entityClass) {
		return toEntity(toString(parameters, parameterName), entityClass);
	}
	
	/**
	 * Converts parameter to given {@code entityClass} from given parameters.
	 * 
	 * @param parameterValue
	 * @param parameterName
	 * @param entityClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends BaseEntity> T toEntity(String parameterValue, Class<T> entityClass) {
	    if(StringUtils.isEmpty(parameterValue)) {
	    	return null;
	    }
		T entity = (T) lookupService.lookupEntity(entityClass, parameterValue);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE, "Entity type [%s] with identifier [%s] does not found", ImmutableMap.of("entityClass", entityClass.getSimpleName(), "identifier", parameterValue));
		}
		return entity;
	}
	
	/**
	 * Converts parameter to entity id.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @param entityClass
	 * @return
	 */
	public UUID toEntityUuid(MultiValueMap<String, Object> parameters, String parameterName, Class<? extends AbstractEntity> entityClass) {
		return toEntityUuid(toString(parameters, parameterName), entityClass);
	}
	
	/**
	 * Converts parameter value to entity id.
	 * 
	 * @param parameterValue
	 * @param entityClass
	 * @return
	 */
	public UUID toEntityUuid(String parameterValue, Class<? extends AbstractEntity> entityClass) {
		AbstractEntity entity = toEntity(parameterValue, entityClass);
		return entity == null ? null : entity.getId();
	}
	
	/**
	 * Converts parameter {@code DateTime} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public DateTime toDateTime(MultiValueMap<String, Object> parameters, String parameterName) {
		Assert.notNull(parameters);
	    //
		return toDateTime(parameters.toSingleValueMap(), parameterName);
	}
	
	/**
	 * Converts parameter {@code DateTime} from given parameters.
	 * 
	 * @param parameters
	 * @param parameterName
	 * @return
	 */
	public DateTime toDateTime(Map<String, Object> parameters, String parameterName) {
		String valueAsString = toString(parameters, parameterName);
		if (valueAsString == null || valueAsString.isEmpty()) {
			return null;
		} else {
			return new DateTime(valueAsString);
		}
	}
}
