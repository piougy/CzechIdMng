package eu.bcvsolutions.idm.core.api.domain;

/**
 * Interface for objects that own external code / identifier or something like this.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 * @since 8.1.0
 */
public interface ExternalCodeable {
	
	String PROPERTY_EXTERNAL_CODE = "externalCode";

	/**
	 * Get external code
	 *
	 * @return
	 */
	String getExternalCode();
	
	/**
	 * Set external code
	 * 
	 * @param externalCode
	 */
	void setExternalCode(String externalCode);
}
