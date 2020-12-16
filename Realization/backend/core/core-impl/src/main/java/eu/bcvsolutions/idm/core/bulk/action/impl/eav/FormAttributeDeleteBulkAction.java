package eu.bcvsolutions.idm.core.bulk.action.impl.eav;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Delete form attribute.
 * 
 * TODO: #437 - add force attribute
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 */
@Component(FormAttributeDeleteBulkAction.NAME)
@Description("Delete form attribute.")
public class FormAttributeDeleteBulkAction extends AbstractRemoveBulkAction<IdmFormAttributeDto, IdmFormAttributeFilter> {

	public static final String NAME = "core-form-attribute-delete-bulk-action";
	//
	@Autowired private IdmFormAttributeService service;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.FORM_ATTRIBUTE_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmFormAttributeDto, IdmFormAttributeFilter> getService() {
		return service;
	}
	
	@Override
	protected boolean requireNewTransaction() {
		return true;
	}
}
