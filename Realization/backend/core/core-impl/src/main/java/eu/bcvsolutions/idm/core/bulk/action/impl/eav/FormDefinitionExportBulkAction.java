package eu.bcvsolutions.idm.core.bulk.action.impl.eav;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractExportBulkAction;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormDefinitionFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Bulk operation to export form definitions
 * 
 * @author Ondrej Husnik
 *
 */
@Enabled(CoreModuleDescriptor.MODULE_ID)
@Component("formDefinitionExportBulkAction")
@Description("Bulk operation to export the form definition.")
public class FormDefinitionExportBulkAction extends AbstractExportBulkAction<IdmFormDefinitionDto, IdmFormDefinitionFilter> {

	public static final String NAME = "form-definition-export-bulk-action";

	
	@Autowired
	private IdmFormDefinitionService formDefinitionService;

	@Override
	protected void exportDto(IdmFormDefinitionDto dto) {
		IdmFormDefinitionDto formDefinitionDto = formDefinitionService.get(dto.getId(), IdmBasePermission.READ);
		initBatch("Form definition export");
		exportFormDefinition(formDefinitionDto);
	}

	private void exportFormDefinition(IdmFormDefinitionDto definition) {
		if (definition != null) {
			formDefinitionService.export(definition.getId(), getBatch());
		}
	}

	@Override
	public List<String> getAuthorities() {
		List<String> authorities = super.getAuthorities();
		authorities.add(CoreGroupPermission.FORM_DEFINITION_READ);
		authorities.add(CoreGroupPermission.EXPORTIMPORT_CREATE);
		authorities.add(CoreGroupPermission.EXPORTIMPORT_READ);
		authorities.add(CoreGroupPermission.EXPORTIMPORT_UPDATE);
		return authorities;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.FORM_DEFINITION_READ);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 1000;
	}

	@Override
	public ReadWriteDtoService<IdmFormDefinitionDto, IdmFormDefinitionFilter> getService() {
		return formDefinitionService;
	}

}
