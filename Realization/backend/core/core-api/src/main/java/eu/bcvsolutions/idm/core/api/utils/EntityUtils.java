/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bcvsolutions.idm.core.api.utils;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * Common entity helpers
 *
 * @author Radek Tomiška <tomiska@ders.cz>
 */
public class EntityUtils {

	private EntityUtils() {
	}
	/**
	 * Returns true, when given entity is valid, false otherwise
	 *
	 * @param entity
	 * @return
	 */
	public static boolean isValid(ValidableEntity entity) {
		if (entity == null) {
			return false;
		}		
		LocalDate now = new LocalDate();		
		return (entity.getValidFrom() == null || entity.getValidFrom().compareTo(now) <= 0)
				&& (entity.getValidTill() == null || entity.getValidTill().compareTo(now) >= 0);
	}
	
	/**
	 * Returns module name by entity package (by convention) 
	 * 
	 * TODO: Does not work for inline classes
	 * 
	 * @param entityClass
	 * @return module identifier
	 */
	public static String getModule(Class<?> entityClass) {
		Assert.notNull(entityClass);
		//
		String name = entityClass.getCanonicalName();
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		String packages[] = name.split("\\.");
		if (packages.length > 3) {
			return packages[3];
		}
		return null;
	}
	
	/**
	 * Returns {@link UUID} from given {@code string} or {@link UUID}.
	 * 
	 * @param identifier {@code string} or {@link UUID} 
	 * @return
	 * @throws  IllegalArgumentException
     *          If identifier does not conform to the string representation as
     *          described in {@link #toString}
	 */
	public static UUID toUuid(Serializable identifier) {
		if (identifier instanceof String) {
			return UUID.fromString((String) identifier);
		}
		return (UUID) identifier;
	}
}
