package eu.bcvsolutions.idm.core.eav.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.dto.filter.FormAttributeFilter;

/**
 * Form attributes definition
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmFormAttributeService 
		extends ReadWriteDtoService<IdmFormAttributeDto, FormAttributeFilter> {
	
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
