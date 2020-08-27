package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.acc.dto.AccPasswordChangeOptionDto;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordFilter;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.CodeableService;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Uniform password definition. Used only for standard crud operation.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public interface AccUniformPasswordService extends//
		 EventableDtoService<AccUniformPasswordDto, AccUniformPasswordFilter>,
		 AuthorizableService<AccUniformPasswordDto>,
		 CodeableService<AccUniformPasswordDto> {

	/**
	 * Prepare password change options for given identity. The options will be united by active uniform password definition.
	 * Given permissions is evaluated for read accounts.
	 *
	 * @param identity
	 * @param permissions
	 * @return
	 */
	List<AccPasswordChangeOptionDto> findOptionsForPasswordChange(IdmIdentityDto identity, BasePermission ...permissions);
}





