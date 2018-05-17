package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Configuration for contract slice
 * 
 * @author svandav
 *
 */
public interface ContractSliceConfiguration extends Configurable {

	String PROPERTY_PROTECTION_INTERVAL = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX
			+ "core.contract-slice.protection-interval";
	String DEFAULT_PROTECTION_INTERVAL = "0";

	@Override
	default String getConfigurableType() {
		return "contract-slice";
	}

	@Override
	default boolean isDisableable() {
		return false;
	}

	@Override
	default public boolean isSecured() {
		return true;
	}

	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>();
		properties.add(getPropertyName(PROPERTY_PROTECTION_INTERVAL));
		return properties;
	}

	/**
	 * Returns protection interval. It is number of days, when contract will be not
	 * terminated, if a next slice exists and diff of his contract valid from (and
	 * current slice contract valid till) is lower then this interval.
	 * 
	 * @return
	 */
	int getProtectionInterval();

}
