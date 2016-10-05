package eu.bcvsolutions.idm.core.api.dto;

import org.springframework.hateoas.Identifiable;

/**
 * Any dto has identifier
 * 
 * @author Radek Tomiška 
 *
 */
public interface BaseDto extends Identifiable<Long> {
	
	/**
	 * Returns indentifier
	 *
	 * @return
	 */
	Long getId();

	/**
	 * Set indentifier
	 *
	 * @param id
	 */
	void setId(Long id);

}
