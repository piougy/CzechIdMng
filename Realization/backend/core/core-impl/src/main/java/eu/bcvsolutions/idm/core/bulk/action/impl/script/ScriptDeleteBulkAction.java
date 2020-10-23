package eu.bcvsolutions.idm.core.bulk.action.impl.script;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptFilter;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Delete given script definition.
 *
 * @author Ondrej Husnik
 * @since 10.6.0
 */
@Component(ScriptDeleteBulkAction.NAME)
@Description("Delete given script definition.")
public class ScriptDeleteBulkAction extends AbstractRemoveBulkAction<IdmScriptDto, IdmScriptFilter> {

	public static final String NAME = "script-definition-delete-bulk-action";

	@Autowired
	private IdmScriptService scriptService;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.SCRIPT_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmScriptDto, IdmScriptFilter> getService() {
		return scriptService;
	}
}
