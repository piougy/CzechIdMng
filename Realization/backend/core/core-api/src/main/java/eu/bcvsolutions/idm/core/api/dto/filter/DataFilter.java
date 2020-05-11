package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Common filter for registerable filter builders - contains filter parameters as map. 
 * Registered filter builders will have all values available.
 * 
 * @see FilterBuilder
 * @see ParameterConverter
 * @author Radek Tomiška
 */
public class DataFilter 
		extends QuickFilter 
		implements BaseDataFilter, ModifiedFromFilter, ModifiedTillFilter, 
			CreatedFromFilter, CreatedTillFilter, PermissionContext {

	/**
	 * Dto uuid identifier
	 */
	public static final String PARAMETER_ID = BaseEntity.PROPERTY_ID;
	
	/**
	 * Codeable identifier - uuid or code
	 */
	public static final String PARAMETER_CODEABLE_IDENTIFIER = "codeable";
	
	/**
	 * "Quick" search parameter
	 */
	public static final String PARAMETER_TEXT = "text";
	
	/**
	 * Transaction id search parameter
	 */
	public static final String PARAMETER_TRANSACTION_ID = Auditable.PROPERTY_TRANSACTION_ID;
	
	/**
	 * Dto class
	 */
	private final Class<? extends BaseDto> dtoClass;
	/**
	 * Underlying filter parameters are stored in data map.
	 */
	@JsonIgnore
	protected final MultiValueMap<String, Object> data;
	/**
	 * Helper to get parameters from data map.
	 */
	@JsonIgnore
	private final ParameterConverter parameterConverter;
	
	public DataFilter(Class<? extends BaseDto> dtoClass) {
		this(dtoClass, null);
	}
	
	public DataFilter(Class<? extends BaseDto> dtoClass, MultiValueMap<String, Object> data) {
		this(dtoClass, data, null);
	}
	
	/**
	 * @param dtoClass
	 * @param data
	 * @param parameterConverter [optional] Fully initialized parameter converted. If no given, default is constructed (without lookup support).
	 * @since 9.6.3
	 */
	public DataFilter(
			Class<? extends BaseDto> dtoClass,
			MultiValueMap<String, Object> data,
			ParameterConverter parameterConverter) {
		this.dtoClass = dtoClass;
		this.data = data != null ? data : new LinkedMultiValueMap<>();
		this.parameterConverter = parameterConverter == null ? new ParameterConverter() : parameterConverter;
	}
	
	/**
	 * Return unmodifiable filtering properties.
	 * 
	 * @return filled properties
	 */
	@Override
	public MultiValueMap<String, Object> getData() {
		return new LinkedMultiValueMap<>(data);
	}
	
	/**
	 * Puts filter parameters into underlying filter data.
	 * 
	 * @param data
	 * @since 9.6.3
	 */
	@Override
	public void putData(MultiValueMap<String, Object> data) {
		this.data.putAll(data);
	}
	
	@Override
	public void set(String propertyName, Object propertyValue) {
		data.set(propertyName, propertyValue);
	}
	
	@Override
	public void put(String propertyName, List<Object> propertyValues) {
		if (CollectionUtils.isEmpty(propertyValues)) {
			remove(propertyName);
		} else {
			data.put(propertyName, propertyValues);
		}
	}
	
	@Override
	public void remove(String propertyName) {
		data.remove(propertyName);
	}
	
	/**
	 * Returns target dto type for this filter
	 * 
	 * @return
	 */
	public Class<? extends BaseDto> getDtoClass() {
		return dtoClass;
	}
	
	/**
	 * Entity identifier
	 * 
	 * @return
	 */
	@Override
	public UUID getId() {
		return EntityUtils.toUuid(data.getFirst(PARAMETER_ID));
	}
	
	@Override
	public void setId(UUID id) {
		if (id == null) {
    		data.remove(PARAMETER_ID);
    	} else {
    		data.put(PARAMETER_ID, Lists.newArrayList(id));
    	}
	}
	
	/**
	 * Entity identifiers.
	 * 
	 * @return
	 * @since 9.7.0
	 */
	public List<UUID> getIds() {
		return getParameterConverter().toUuids(data, PARAMETER_ID);
	}
    
	/**
	 * Entity identifiers.
	 * 
	 * @param ids
	 * @since 9.7.0
	 */
    public void setIds(List<UUID> ids) {
    	if (CollectionUtils.isEmpty(ids)) {
    		data.remove(PARAMETER_ID);
    	} else {
    		data.put(PARAMETER_ID, Lists.newArrayList(ids));
    	}
	}
	
	@Override
	public String getText() {
		return getParameterConverter().toString(data, PARAMETER_TEXT);
	}
	
	@Override
	public void setText(String text) {
		set(PARAMETER_TEXT, text);
	}
	
	public String getCodeableIdentifier() {
		return getParameterConverter().toString(data, PARAMETER_CODEABLE_IDENTIFIER);
	}
	
	public void setCodeableIdentifier(String text) {
		set(PARAMETER_CODEABLE_IDENTIFIER, text);
	}
	
	/**
	 * Transaction id search parameter - can be used for search all abstract dtos.
	 * 
	 * @return
	 * @since 9.5.0
	 */
	public UUID getTransactionId() {
		return getParameterConverter().toUuid(data, PARAMETER_TRANSACTION_ID);
	}
	
	/**
	 * Transaction id search parameter - can be used for search all abstract dtos.
	 * 
	 * @param transactionId
	 * @since 9.5.0
	 */
	public void setTransactionId(UUID transactionId) {
		set(PARAMETER_TRANSACTION_ID, transactionId);
	}
	
	/**
	 * Parameter converter.
	 * 
	 * @return
	 * @since 9.6.3
	 */
	@Override
	public ParameterConverter getParameterConverter() {
		return parameterConverter;
	}
}
