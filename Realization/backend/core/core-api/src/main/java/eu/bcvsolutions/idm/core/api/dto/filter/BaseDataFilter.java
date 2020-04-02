package eu.bcvsolutions.idm.core.api.dto.filter;

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
