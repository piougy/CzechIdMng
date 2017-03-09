package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;

/**
 * Any dto has identifier
 * 
 * @author Radek Tomiška 
 *
 */
public interface BaseDto {
	
	/**
	 * Returns indentifier
	 *
	 * @return
	 */
	Serializable getId();

	/**
	 * Set indentifier
	 *
	 * @param id
	 */
	void setId(Serializable id);

}
