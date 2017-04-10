package eu.bcvsolutions.idm.core.api.entity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

/**
 * Entity with validity
 * 
 * @author Radek Tomiška 
 *
 */
public interface ValidableEntity {
	
	/**
	 * Default constant for joda time (Long.MAX and Long.MIN doesn't work)
	 */
	static final public DateTime MIN_TIME = new DateTime( 0000, 1, 1, 0, 0, 0, DateTimeZone.UTC);
    static final public DateTime MAX_TIME = new DateTime( 9999, 1, 1, 0, 0, 0, DateTimeZone.UTC);

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
}
