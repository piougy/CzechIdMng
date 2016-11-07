package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import org.springframework.hateoas.Identifiable;

/**
 * Any dto has identifier
 * 
 * @author Radek Tomiška 
 *
 */
public interface BaseDto extends Identifiable<UUID> {
	
	/**
	 * Returns indentifier
	 *
	 * @return
	 */
	UUID getId();

	/**
	 * Set indentifier
	 *
	 * @param id
	 */
	void setId(UUID id);

}
