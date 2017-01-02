package eu.bcvsolutions.idm.ic.api;

import java.util.List;

/**
 * Keep configuration properties for IC connector
 * @author svandav
 *
 */
public interface IcConfigurationProperties {

	/**
	 * The list of properties {@link IcConfigurationProperty}.
	 */
	List<IcConfigurationProperty> getProperties();

}