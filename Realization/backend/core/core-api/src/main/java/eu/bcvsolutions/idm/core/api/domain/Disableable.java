package eu.bcvsolutions.idm.core.api.domain;

/**
 * Entity could be disabled
 * 
 * @author Radek Tomiška
 */
public interface Disableable {

	/**
	 * Returns true, when entity is disabled
     *
	 * @return
	 */
	boolean isDisabled();

	/**
	 * Enable / disable entity
	 * 
	 * @param disabled
	 */
	void setDisabled(boolean disabled);
}
