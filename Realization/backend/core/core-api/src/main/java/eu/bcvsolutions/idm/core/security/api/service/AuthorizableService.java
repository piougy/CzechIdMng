package eu.bcvsolutions.idm.core.security.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Sevrvice supports authorizationevaluation.
 * 
 * @author Radek Tomiška
 *
 */
public interface AuthorizableService<E extends BaseEntity, F extends BaseFilter> {

	/**
	 * Secured type
	 * 
	 * @return
	 */
	AuthorizableType getAuthorizableType();
	
	/**
	 * Returns data by authorization polices
	 * 
	 * @param filter
	 * @param permission evaluate permission
	 * @param pageable
	 * @return
	 */
	Page<E> findSecured(F filter, BasePermission permission, Pageable pageable);
	
}
