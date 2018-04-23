package eu.bcvsolutions.idm.core.api.domain;

/**
 * Interface for objects that own external code/identificator or something like this.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface ExternalCodeable {

	/**
	 * Get external code
	 *
	 * @return
	 */
	String getExternalCode();
}
