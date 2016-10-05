/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.bcvsolutions.idm.core.api.utils;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

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
		return (entity.getValidFrom() == null || DateUtils.truncate(entity.getValidFrom(), Calendar.DATE).compareTo(DateUtils.truncate(new Date(), Calendar.DATE)) <= 0)
				&& (entity.getValidTill() == null || DateUtils.truncate(entity.getValidTill(), Calendar.DATE).compareTo(DateUtils.truncate(new Date(), Calendar.DATE)) >= 0);
	}
}
