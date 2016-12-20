package eu.bcvsolutions.idm.icf.api;

import java.util.List;

/**
 * Keep configuration properties for ICF connector
 * @author svandav
 *
 */
public interface IcfConfigurationProperties {

	/**
	 * The list of properties {@link IcfConfigurationProperty}.
	 */
	List<IcfConfigurationProperty> getProperties();

}