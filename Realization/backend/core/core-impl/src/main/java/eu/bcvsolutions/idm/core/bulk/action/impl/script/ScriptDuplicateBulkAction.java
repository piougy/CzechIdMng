package eu.bcvsolutions.idm.core.bulk.action.impl.script;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Duplicate script definition.
 *
 * @author Ondrej Husnik
 * @since 11.0.0
 */
@Component(ScriptDuplicateBulkAction.NAME)
@Description("Duplicate script.")
public class ScriptDuplicateBulkAction extends AbstractBulkAction<IdmScriptDto, IdmScriptFilter> {

	public static final String NAME = "core-duplicate-script-bulk-action";

	@Autowired private IdmScriptService scriptService;

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 100;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.SCRIPT_CREATE, CoreGroupPermission.SCRIPT_READ);
	}
	
	@Override
	public ReadWriteDtoService<IdmScriptDto, IdmScriptFilter> getService() {
		return scriptService;
	}
	
	@Override
	protected OperationResult processDto(IdmScriptDto dto) {
		Assert.notNull(dto, "Script is required!");
		scriptService.checkAccess(dto, IdmBasePermission.READ, IdmBasePermission.CREATE);
		scriptService.duplicate(dto.getId());
		
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}	
}
