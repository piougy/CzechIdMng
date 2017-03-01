package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.UUID;

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
	void setId(UUID id);

}
