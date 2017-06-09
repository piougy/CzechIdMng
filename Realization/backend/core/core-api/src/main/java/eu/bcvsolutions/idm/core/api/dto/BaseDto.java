package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;

/**
 * Any dto has identifier
 * 
 * @author Radek Tomiška 
 */
public interface BaseDto extends Identifiable, Serializable {
	
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
