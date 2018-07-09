package eu.bcvsolutions.idm.core.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTokenFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmTokenService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import io.swagger.annotations.Api;

/**
 * Created IdM tokens
 * - for admin only
 * 
 * @author Radek Tomiška
 *
 */
@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/tokens")
@Api(
		value = IdmTokenController.TAG, 
		description = "Operations with IdM tokens", 
		tags = { IdmTokenController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmTokenController extends DefaultReadWriteDtoController<IdmTokenDto, IdmTokenFilter> {
	
	protected static final String TAG = "Tokens";
	
	@Autowired
	public IdmTokenController(IdmTokenService service) {
		super(service);
	}
	
	@Override
	protected IdmTokenFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new IdmTokenFilter(parameters);
	}
}
