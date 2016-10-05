package eu.bcvsolutions.idm.core.api.entity;

import java.util.Date;

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
	Date getValidFrom();
	
	/**
	 * Entity is valid till date
	 * 
	 * @return 
	 */
	Date getValidTill();
}
