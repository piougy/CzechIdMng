package eu.bcvsolutions.idm.core.api.entity;

import org.joda.time.LocalDate;

/**
 * Entity with validity
 * 
 * @author Radek Tomiška 
 *
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
}
