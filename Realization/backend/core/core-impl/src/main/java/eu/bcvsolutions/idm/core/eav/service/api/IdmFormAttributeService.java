package eu.bcvsolutions.idm.core.eav.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.eav.dto.filter.FormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;

/**
 * Form attributes definition
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmFormAttributeService extends ReadWriteEntityService<IdmFormAttribute, FormAttributeFilter> {
	
	/**
	 * Finds one attribute from given definition by given attribute name
	 * 
	 * @param definitionType
	 * @param definitionName
	 * @param attributeName
	 * @return
	 */
	IdmFormAttribute findAttribute(String definitionType, String definitionName, String attributeName);

}
