package eu.bcvsolutions.idm.core.api.domain;

/**
 * Interface for objects that own external identifier or something like this.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 * @since 8.1.0
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
