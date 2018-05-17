package eu.bcvsolutions.idm.core.config.domain;

import com.google.common.base.Strings;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.ContractSliceConfiguration;

/**
 * Configuration for contract slice
 * 
 * @author svandav
 *
 */
public class DefaultContractSliceConfiguration extends AbstractConfiguration implements ContractSliceConfiguration {	

	@Override
	public int getProtectionInterval() {
		String protectionInterval = getConfigurationService().getValue(PROPERTY_PROTECTION_INTERVAL, DEFAULT_PROTECTION_INTERVAL);
		if(Strings.isNullOrEmpty(protectionInterval)) {
			return 0;
		}
		return Integer.parseInt(protectionInterval);
	}
}
