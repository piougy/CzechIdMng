package eu.bcvsolutions.idm.core.api.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.quartz.JobDataMap;

import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * Common configuration properties.
 * <p>
 * JobDataMap from quartz is reused (we want the same functionality).
 * Use {@link Serializable} as values.
 * 
 * @author Radek Tomiška
 */
public class ConfigurationMap extends JobDataMap {

	private static final long serialVersionUID = 7431983283346202466L;

	/**
     * <p>
     * Retrieve the identified <code>UUID</code> value from the <code>StringKeyDirtyFlagMap</code>.
     * </p>
     * 
     * @throws ClassCastException if the identified object is not an UUID.
     */
	public UUID getUuid(String key) {
        return EntityUtils.toUuid(get(key));
    }
	
	/**
	 * Converts configuration properties to map of Serializable values.
	 * Use {@link Serializable} as values, throws {@link ClassCastException} otherwise.
	 * 
	 * @return
	 */
	public Map<String, Serializable> toMap() {
		Map<String, Serializable> properties = new HashMap<>();
		this.forEach((k, v) -> {
			properties.put(k, (Serializable) v);
		});
		return properties;
	}
}
