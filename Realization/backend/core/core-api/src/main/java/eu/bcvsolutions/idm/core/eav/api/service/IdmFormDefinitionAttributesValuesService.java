package eu.bcvsolutions.idm.core.eav.api.service;

import java.util.List;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * From attribute values
 *
 * @author Roman Kučera
 */
public interface IdmFormDefinitionAttributesValuesService extends
		ReadWriteDtoService<IdmFormValueDto, IdmFormValueFilter>, AuthorizableService<IdmFormValueDto> {

	/**
	 * Find all attributes values for given form definition
	 * @return
	 */
//	List<IdmFormValueDto> findDefinitionAttributesValues(String definitionId);
}
