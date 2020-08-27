package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordSystemFilter;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for connection between {@link AccUniformPasswordDto} and {@link SysSystemDto}.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public interface AccUniformPasswordSystemService extends//
		EventableDtoService<AccUniformPasswordSystemDto, AccUniformPasswordSystemFilter>,
		AuthorizableService<AccUniformPasswordSystemDto> {

}
