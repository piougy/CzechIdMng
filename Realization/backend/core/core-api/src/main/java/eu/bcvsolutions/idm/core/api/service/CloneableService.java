package eu.bcvsolutions.idm.core.api.service;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;

/**
 * Entity can be clone by given UUID
 * 
 * @author Svanda
 *
 * @param <I>
 */
public interface CloneableService<I extends Identifiable> {
	
	
	/**
	 * Clone entity without connected entities (does not create (persist) a new entity)
	 * @param id
	 * @return
	 */
	I clone(UUID id);
}
