package eu.bcvsolutions.idm.ic.api;

import java.io.Serializable;

/**
 * Elementary configuration property for connector
 * @author svandav
 *
 */
public interface IcConfigurationProperty extends Serializable {

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
	
	int getOrder();

	/**
	 * Define how will be property rendered
	 * @return
	 */
	String getFace();


}