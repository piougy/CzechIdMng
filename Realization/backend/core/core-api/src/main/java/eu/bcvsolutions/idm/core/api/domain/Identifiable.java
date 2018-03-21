package eu.bcvsolutions.idm.core.api.domain;

import java.io.Serializable;

/**
 * Common object with id
 * 
 * @author Radek Tomiška
 */
public interface Identifiable extends Serializable {
	
	/**
	 * Returns indentifier
	 *
	 * @return
	 */
	Serializable getId();
}
