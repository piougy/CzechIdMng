package eu.bcvsolutions.idm.core.api.dto.filter;

/**
 * Filter for search entity by correlation property
 *
 * @author svandav
 */
public interface CorrelationFilter extends BaseFilter {

	
	/**
	 * Little dynamic search by identity property and value
	 */
	public static final String PARAMETER_CORRELATION_PROPERTY = "correlationProperty";
	public static final String PARAMETER_CORRELATION_VALUE = "correlationValue";
	
    String getProperty();

    void setProperty(String property);

    String getValue();

    void setValue(String value);

}