package eu.bcvsolutions.idm.core.api.dto.filter;

/**
 * Filter for form-able entities
 *
 * @author svandav
 */
public interface FormableFilter extends BaseFilter {

	// If is true, then will be to the result added EAV metadata (it means FormInstances) 
	String PARAMETER_ADD_EAV_METADATA = "addEavMetadata";
	
    Boolean getAddEavMetadata();

    void setAddEavMetadata(Boolean property);

}