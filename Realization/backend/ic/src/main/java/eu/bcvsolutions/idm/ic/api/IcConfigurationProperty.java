package eu.bcvsolutions.idm.ic.api;

/**
 * Elementary configuration property for connector
 * @author svandav
 *
 */
public interface IcConfigurationProperty {

	/**
	 * @return the name
	 */
	String getName();

	/**
	 * @return the helpMessage
	 */
	String getHelpMessage();

	/**
	 * @return the displayName
	 */
	String getDisplayName();

	/**
	 * @return the group
	 */
	String getGroup();


	/**
	 * @return the value
	 */
	Object getValue();

	/**
	 * @return the type
	 */
	String getType();


	/**
	 * @return the confidential
	 */
	boolean isConfidential();

	/**
	 * @return the required
	 */
	boolean isRequired();


}