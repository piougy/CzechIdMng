package eu.bcvsolutions.idm.core.api.domain;

import java.util.UUID;

import org.quartz.JobDataMap;

/**
 * Common configuration properties.
 * 
 * JobDataMap from quartz is reused (we want the same functionality)
 * 
 * @author Radek Tomiška
 *
 */
public class ConfigurationMap extends JobDataMap {

	private static final long serialVersionUID = 7431983283346202466L;

	/**
     * <p>
     * Retrieve the identified <code>UUID</code> value from the <code>StringKeyDirtyFlagMap</code>.
     * </p>
     * 
     * @throws ClassCastException
     *           if the identified object is not an UUID.
     */
	public UUID getUuid(String key) {
        Object obj = get(key);
    
        try {
            if(obj instanceof UUID) {
                return (UUID) obj;
            }
            return UUID.fromString((String)obj);
        } catch (Exception e) {
            throw new ClassCastException("Identified object is not an UUID.");
        }
    }
}
