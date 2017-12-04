package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;

/**
 * Any dto has identifier
 * 
 * TODO" template fot id type
 * 
 * @author Radek Tomiška 
 */
public interface BaseDto extends Identifiable, Serializable {
	
	/**
	 * Returns identifier
	 *
	 * @return
	 */
	Serializable getId();

	/**
	 * Set identifier
	 *
	 * @param id
	 */
	void setId(Serializable id);

}
