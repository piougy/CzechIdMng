package eu.bcvsolutions.idm.core.api.entity;

import org.joda.time.LocalDate;
import org.springframework.util.Assert;

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
	 * @return
	 */
	default boolean isValid() {
		return isValid(LocalDate.now());
	}
	
	/**
	 * Returns if entity is valid for given date.
	 * @param targetDate
	 * @return
	 */
	default boolean isValid(LocalDate targetDate) {
		Assert.notNull(targetDate);
		//
		LocalDate from = getValidFrom();
		LocalDate till = getValidTill();
		boolean fromValid = from == null || from.isEqual(targetDate) || from.isBefore(targetDate);
		boolean tillValid = till == null || till.isEqual(targetDate) || till.isAfter(targetDate);
		return fromValid && tillValid;
	}
	
}
