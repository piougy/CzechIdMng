package eu.bcvsolutions.idm.core.eav.api.service;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;

/**
 * Form attributes definition
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmFormAttributeService 
		extends ReadWriteDtoService<IdmFormAttributeDto, IdmFormAttributeFilter> {
	
	/**
	 * Finds one attribute from given definition by given attribute name
	 * 
	 * @param definitionType
	 * @param definitionCode
	 * @param attributeCode
	 * @return
	 */
	IdmFormAttributeDto findAttribute(String definitionType, String definitionCode, String attributeCode);

}
