package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Entity is identifiable by id and name
 * 
 * @author Radek Tomiška
 *
 * @param <I>
 */
public interface CodeableService<I extends Identifiable> {
	
	/**
	 * Returns entity by given id
	 * 
	 * @param id
	 * @return
	 */
	I get(Serializable id, BasePermission... permission);
	
	/**
	 * Return identifiable object by given code
	 * 
	 * @param code
	 * @return
	 */
	I getByCode(String name);
}
