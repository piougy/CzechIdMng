package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.List;

import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Common filter for registerable filter builders - contains filter parameters as map. 
 * Registered filter builders will have all values available.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
public interface BaseDataFilter extends BaseFilter {

	/**
	 * Parameter converter.
	 * 
	 * @return converter helper
	 */
	ParameterConverter getParameterConverter();
	
	/**
	 * Set property.
	 * 
	 * @param propertyName
	 * @param propertyValue
	 */
	void set(String propertyName, Object propertyValue);
	
	/**
	 * Put all property values.
	 * If values are empty (null or empty list), then paremater is removed form filter.
	 * 
	 * @param propertyName
	 * @param propertyValues
	 * @since 10.3.0
	 */
	void put(String propertyName, List<Object> propertyValues);
	
	/**
	 * Remove all property values.
	 * 
	 * @param propertyName
	 * @since 10.3.0
	 */
	void remove(String propertyName);
	
	/**
	 * Puts filter parameters into underlying filter data.
	 * 
	 * @param data
	 */
	void putData(MultiValueMap<String, Object> data);
	
	/**
	 * Get filled properties.
	 * 
	 * @return filled filter properties
	 */
	MultiValueMap<String, Object> getData();
}
