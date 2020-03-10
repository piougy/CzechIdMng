package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;

/**
 * Filter for objects that own external identifier.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
public interface ExternalIdentifiableFilter extends ExternalIdentifiable, BaseDataFilter {

	/**
	 * Get external identifier.
	 *
	 * @return
	 */
	@Override
	default public String getExternalId() {
		return getParameterConverter().toString(getData(), PROPERTY_EXTERNAL_ID);
	}
	
	/**
	 * Set external identifier.
	 * 
	 * @param externalId
	 */
	@Override
	default public void setExternalId(String externalId) {
		set(PROPERTY_EXTERNAL_ID, externalId);
	}
}
