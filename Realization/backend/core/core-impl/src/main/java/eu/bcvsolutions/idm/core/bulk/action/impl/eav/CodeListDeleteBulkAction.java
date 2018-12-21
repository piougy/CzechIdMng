package eu.bcvsolutions.idm.core.bulk.action.impl.eav;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmCodeListFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmCodeListService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Delete code lists
 * 
 * @author Radek Tomiška
 *
 */
@Component(CodeListDeleteBulkAction.NAME)
@Description("Delete code lists.")
public class CodeListDeleteBulkAction extends AbstractRemoveBulkAction<IdmCodeListDto, IdmCodeListFilter> {

	public static final String NAME = "core-code-list-delete-bulk-action";
	//
	@Autowired private IdmCodeListService service;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.CODE_LIST_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmCodeListDto, IdmCodeListFilter> getService() {
		return service;
	}
}
