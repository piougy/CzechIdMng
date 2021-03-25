package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.List;

import eu.bcvsolutions.idm.core.eav.api.dto.FormDefinitionAttributes;

/**
 * Context (~filter) for form-able entities.
 *
 * @author svandav
 * @author Radek Tomiška
 */
public interface FormableFilter extends BaseDataFilter {

	/**
	 * If is true, then will be to the result added EAV metadata (it means FormInstances).
	 * Parameter {@link #PARAMETER_FORM_ATTRIBUTE} can be used to.
	 */
	String PARAMETER_ADD_EAV_METADATA = "addEavMetadata";
	/**
	 * Find given form form definitions and attributes only.
	 * 
	 * @since 10.3.0
	 */
	String PARAMETER_FORM_DEFINITON_ATTRIBUTES = "formDefinitionAttributes";
	/**
	 * Load system (internal) form definition for owner type basic fields for validations and additional configurations.
	 * 
	 * @since 11.0.0
	 */
	String PARAMETER_ADD_BASIC_FIELDS = "addBasicFields";
	
	/**
	 * Load extended attributes after filter is applied. 
	 * 
	 * @return true - extended attributes will be loaded
	 */
    default Boolean getAddEavMetadata() {
    	return getParameterConverter().toBoolean(getData(), PARAMETER_ADD_EAV_METADATA);
    }

    /**
     * Load extended attributes after filter is applied.
     * 
     * @param value true - extended attributes will be loaded
     */
    default void setAddEavMetadata(Boolean value) {
    	set(PARAMETER_ADD_EAV_METADATA, value);
    }
    
    /**
	 * Find given form form definitions and attributes only.
	 * 
	 * @return given form definition and attributes are needed to find only
	 * @since 10.3.0
	 */
    @SuppressWarnings("unchecked")
	default List<FormDefinitionAttributes> getFormDefinitionAttributes() {
    	return (List<FormDefinitionAttributes>)(Object) getData().get(PARAMETER_FORM_DEFINITON_ATTRIBUTES);
    }
    
    /**
	 * Find given form form definitions and attributes only.
	 * 
	 * @param attributes given form definition and attributes are needed to find only
	 * @since 10.3.0
	 */
	default void setFormDefinitionAttributes(List<FormDefinitionAttributes> attributes) {
    	put(PARAMETER_FORM_DEFINITON_ATTRIBUTES, attributes);
    }
	
	/**
	 * Load system (internal) form definition for owner type basic fields for validations and additional configurations.
	 * 
	 * @return true - append basic fields form definition
	 * @since 11.0.0
	 */
	default boolean isAddBasicFields() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_ADD_BASIC_FIELDS, false);
	}
	
	/**
	 * Load system (internal) form definition for owner type basic fields for validations and additional configurations.
	 * 
	 * @param addBasicFields true - append basic fields form definition
	 * @since 11.0.0
	 */
	default void setAddBasicFields(boolean addBasicFields) {
		set(PARAMETER_ADD_BASIC_FIELDS, addBasicFields);
	}
}