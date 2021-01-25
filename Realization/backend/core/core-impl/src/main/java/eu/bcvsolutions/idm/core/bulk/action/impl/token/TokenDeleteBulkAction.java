package eu.bcvsolutions.idm.core.bulk.action.impl.token;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTokenFilter;
import eu.bcvsolutions.idm.core.api.service.IdmTokenService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Delete given token.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
@Component(TokenDeleteBulkAction.NAME)
@Description("Delete given token.")
public class TokenDeleteBulkAction extends AbstractRemoveBulkAction<IdmTokenDto, IdmTokenFilter> {

	public static final String NAME = "core-token-delete-bulk-action";

	@Autowired private IdmTokenService tokenService;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.TOKEN_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmTokenDto, IdmTokenFilter> getService() {
		return tokenService;
	}
}
