package eu.bcvsolutions.idm.core.bulk.action.impl.eav;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListItemDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmCodeListItemFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmCodeListItemService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Delete code list items.
 * 
 * @author Radek Tomiška
 *
 */
@Component(CodeListItemDeleteBulkAction.NAME)
@Description("Delete code list items.")
public class CodeListItemDeleteBulkAction extends AbstractRemoveBulkAction<IdmCodeListItemDto, IdmCodeListItemFilter> {

	public static final String NAME = "core-code-list-item-delete-bulk-action";
	//
	@Autowired private IdmCodeListItemService service;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.CODE_LIST_ITEM_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmCodeListItemDto, IdmCodeListItemFilter> getService() {
		return service;
	}
}
