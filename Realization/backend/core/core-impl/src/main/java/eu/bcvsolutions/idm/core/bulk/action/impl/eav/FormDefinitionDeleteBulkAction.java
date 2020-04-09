package eu.bcvsolutions.idm.core.bulk.action.impl.eav;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormDefinitionFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Delete form definition
 * 
 * @author Ondrej Husnik
 *
 */
@Component(FormDefinitionDeleteBulkAction.NAME)
@Description("Delete form definition.")
public class FormDefinitionDeleteBulkAction extends AbstractRemoveBulkAction<IdmFormDefinitionDto, IdmFormDefinitionFilter> {

	public static final String NAME = "form-definition-delete-bulk-action";
	//
	@Autowired private IdmFormDefinitionService service;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.FORM_DEFINITION_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmFormDefinitionDto, IdmFormDefinitionFilter> getService() {
		return service;
	}
}
