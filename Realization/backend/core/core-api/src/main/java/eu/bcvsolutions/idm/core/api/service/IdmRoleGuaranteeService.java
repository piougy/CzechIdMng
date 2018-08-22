package eu.bcvsolutions.idm.core.api.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with role guarantees - by identity
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleGuaranteeService extends
	EventableDtoService<IdmRoleGuaranteeDto, IdmRoleGuaranteeFilter>,
	AuthorizableService<IdmRoleGuaranteeDto> {
	
	/**
	 * Role guarantees by role
	 * 
	 * @param roleId
	 * @param pageable
	 * @param permission
	 * @return
	 * @since 9.0.0
	 */
	Page<IdmRoleGuaranteeDto> findByRole(UUID roleId, Pageable pageable, BasePermission... permission);
	
}
