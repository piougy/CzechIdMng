package eu.bcvsolutions.idm.core.api.dto.filter;

/**
 * Filter for search entity by correlation property
 *
 * @author svandav
 */
public interface CorrelationFilter extends BaseFilter {

    String getProperty();

    void setProperty(String property);

    String getValue();

    void setValue(String value);

}