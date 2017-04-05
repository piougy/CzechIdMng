package eu.bcvsolutions.idm.core.security.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Service supports authorization evaluation.
 * 
 * @author Radek Tomiška
 * 
 * 
 * @deprecated use dtos and {@link AuthorizableService}
 */
@Deprecated
public interface AuthorizableEntityService<E extends Identifiable, F extends BaseFilter> extends AuthorizableService<E, F> {
	
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
