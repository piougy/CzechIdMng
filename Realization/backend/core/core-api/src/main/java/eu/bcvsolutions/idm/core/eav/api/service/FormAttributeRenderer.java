package eu.bcvsolutions.idm.core.eav.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.Ordered;

import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;

/**
 * Form attribute renderer - attribute face type with custom configuration.
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 */
public interface FormAttributeRenderer extends Ordered, Configurable {
	
	@Override
	default String getConfigurableType() {
		return "form-attribute-renderer";
	}
	
	/**
	 *  bean name / unique identifier (spring bean name)
	 *  
	 * @return
	 */
	String getId();
	
	/**
	 * Returns persistent type, which supports this renderer.
	 * 
	 * @return
	 */
	PersistentType getPersistentType();
	
	/**
	 * Returns true, when renderer supports given persistent type
	 * 
	 * @param persistentType
	 * @return
	 */
	boolean supports(PersistentType persistentType);
	
	/**
	 * Returns configuration property names for this configurable object
	 */
	@Override
	default List<String> getPropertyNames() {
		return new ArrayList<>();
	}
}
