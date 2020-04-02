package eu.bcvsolutions.idm.core.api.dto.filter;

/**
 * Context (~filter) for form-able entities.
 *
 * @author svandav
 * @author Radek Tomiška
 */
public interface FormableFilter extends BaseDataFilter {

	// If is true, then will be to the result added EAV metadata (it means FormInstances) 
	String PARAMETER_ADD_EAV_METADATA = "addEavMetadata";
	
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

}