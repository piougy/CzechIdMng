package eu.bcvsolutions.idm.eav.service;

import java.util.List;

import eu.bcvsolutions.idm.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.eav.entity.FormableEntity;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;

/**
 * Work with form definitions, attributes and their values
 * 
 * @author Radek Tomiška
 *
 */
public interface FormService {

	/**
	 * Finds definition by given type and name (optional) 
	 * @param type
	 * @param name
	 * @return
	 */
	IdmFormDefinition getDefinition(String type, String name);
		
	<O extends FormableEntity, E extends AbstractFormValue<O>> void saveValues(O owner, List<E> values);
	
	<O extends FormableEntity> List<AbstractFormValue<O>> getValues(O owner);

	<O extends FormableEntity> List<AbstractFormValue<O>> getValues(O owner, IdmFormDefinition formDefinition);
	
	<O extends FormableEntity> void deleteValues(O owner);
	
	<O extends FormableEntity> void deleteValues(O owner, IdmFormDefinition formDefinition);
}
