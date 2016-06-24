package eu.bcvsolutions.idm.core.model.dto;

import org.springframework.hateoas.Identifiable;

/**
 * Any dto has identifier
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
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
