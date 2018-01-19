package eu.bcvsolutions.idm.core.api.entity;

import org.joda.time.LocalDate;

import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * Entity (or dto) with validity
 * 
 * @author Radek Tomiška 
 */
public interface ValidableEntity {
	
	String PROPERTY_VALID_FROM = "validFrom";
	String PROPERTY_VALID_TILL = "validTill";
	
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
	
	/**
	 * Returns true, if entity is valid now or in future.
	 * 
	 * @return
	 */
	default boolean isValidNowOrInFuture() {
		return EntityUtils.isValidNowOrInFuture(this);
	}
	
}
