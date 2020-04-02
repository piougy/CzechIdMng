package eu.bcvsolutions.idm.core.api.dto.filter;

/**
 * Filter for search entity by correlation property.
 *
 * @author svandav
 * @author Radek Tomiška
 */
public interface CorrelationFilter extends BaseDataFilter {

	/**
	 * Little dynamic search by identity property and value
	 */
	String PARAMETER_CORRELATION_PROPERTY = "correlationProperty";
	String PARAMETER_CORRELATION_VALUE = "correlationValue";
	
	/**
	 * Property name (~ dto/entity field).
	 * 
	 * @return property name
	 */
    default String getProperty() {
    	return getParameterConverter().toString(getData(), PARAMETER_CORRELATION_PROPERTY);
    }

    /**
     * Property name (~ dto/entity field).
     * 
     * @param property property name
     */
    default void setProperty(String property) {
    	set(PARAMETER_CORRELATION_PROPERTY, property);
    }

    /**
     * Property value (~ dto/entity field value).
     * 
     * @return value
     */
    default String getValue() {
    	return getParameterConverter().toString(getData(), PARAMETER_CORRELATION_VALUE);
    }

    /**
     * Property value (~ dto/entity field value).
     * 
     * @param value value
     */
    default void setValue(String value) {
    	set(PARAMETER_CORRELATION_VALUE, value);
    }

}