package eu.bcvsolutions.idm.core.bulk.action.impl.token;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTokenFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmTokenService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;

/**
 * Disable given token.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
@Enabled(CoreModuleDescriptor.MODULE_ID)
@Component(TokenDisableBulkAction.NAME)
@Description("Disable given token.")
public class TokenDisableBulkAction extends AbstractBulkAction<IdmTokenDto, IdmTokenFilter> {

	public static final String NAME = "core-token-disable-bulk-action";
	
	@Autowired private IdmTokenService tokenService;
	@Autowired private TokenManager tokenManager;
	
	@Override
	protected OperationResult processDto(IdmTokenDto dto) {
		dto = tokenManager.disableToken(dto.getId(), IdmBasePermission.DELETE);
		//
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.TOKEN_DELETE);
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 100;
	}
	
	@Override
	public NotificationLevel getLevel() {
		return NotificationLevel.WARNING;
	}

	@Override
	public ReadWriteDtoService<IdmTokenDto, IdmTokenFilter> getService() {
		return tokenService;
	}
}
