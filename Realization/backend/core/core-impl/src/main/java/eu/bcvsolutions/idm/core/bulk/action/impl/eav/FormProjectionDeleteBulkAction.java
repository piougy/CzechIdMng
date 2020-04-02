package eu.bcvsolutions.idm.core.bulk.action.impl.eav;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormProjectionFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormProjectionService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Delete form projection.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
@Component(FormProjectionDeleteBulkAction.NAME)
@Description("Delete form projections.")
public class FormProjectionDeleteBulkAction extends AbstractRemoveBulkAction<IdmFormProjectionDto, IdmFormProjectionFilter> {

	public static final String NAME = "core-form-projection-delete-bulk-action";
	//
	@Autowired private IdmFormProjectionService service;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.FORM_PROJECTION_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmFormProjectionDto, IdmFormProjectionFilter> getService() {
		return service;
	}
}
