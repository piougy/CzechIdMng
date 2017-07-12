package eu.bcvsolutions.idm.core.api.entity;

import org.joda.time.LocalDate;

import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * Entity (or dto) with validity
 * 
 * @author Radek Tomiška 
 */
public interface ValidableEntity {
	
	/**
	 * Entity is valid from date
	 * 
	 * @return 
	 */
	LocalDate getValidFrom();
	
	/**
	 * Entity is valid till date
	 * 
	 * @return 
	 */
	LocalDate getValidTill();
	
	/**
	 * Returns if entity is valid today.
	 * 
	 * @return
	 */
	default boolean isValid() {
		return isValid(new LocalDate());
	}
	
	/**
	 * Returns if entity is valid for given date.
	 * 
	 * @param targetDate
	 * @return
	 */
	default boolean isValid(LocalDate targetDate) {
		return EntityUtils.isValid(this, targetDate);
	}
	
}
