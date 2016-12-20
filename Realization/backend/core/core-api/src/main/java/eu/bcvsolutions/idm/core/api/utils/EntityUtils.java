/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bcvsolutions.idm.core.api.utils;

import org.joda.time.LocalDate;

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
}
