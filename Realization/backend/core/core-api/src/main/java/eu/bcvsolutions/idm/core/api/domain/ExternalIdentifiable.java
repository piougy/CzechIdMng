package eu.bcvsolutions.idm.core.api.domain;

/**
 * Interface for objects that own external identifier or something like this.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface ExternalIdentifiable {

	String PROPERTY_EXTERNAL_ID = "externalId";
	
	/**
	 * Get external identifier
	 *
	 * @return
	 */
	String getExternalId();
	
	/**
	 * Set external identifier
	 * 
	 * @param externalId
	 */
	void setExternalId(String externalId);
}
