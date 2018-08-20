package eu.bcvsolutions.idm.core.config.domain;

import java.text.MessageFormat;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.RequestConfiguration;
import eu.bcvsolutions.idm.core.api.domain.Requestable;

/**
 * Configuration for requests.
 * 
 * @author svandav
 *
 */
@Service(value = "requestConfiguration")
public class DefaultRequestConfiguration extends AbstractConfiguration implements RequestConfiguration {

	@Override
	public boolean isRoleRequestEnabled() {
		return getConfigurationService().getBooleanValue(PROPERTY_ROLE_ENABLE, false);
	}

	@Override
	public String getRequestApprovalProcessKey(Class<Requestable> entityType) {
		if (entityType == null) {
			return null;
		}
		String entityNameCamel = entityType.getSimpleName();
		StringBuilder entityNameBuilder = new StringBuilder();

		for (String word : entityNameCamel.split(CAMEL_SPLIT_REGEX)) {
			if(word.equalsIgnoreCase("dto")) {
				continue;
			}
			entityNameBuilder.append(word.toLowerCase());
			entityNameBuilder.append('-');
		}
		String entityName = entityNameBuilder.toString();
		entityName = entityName.substring(0, entityName.length() - 1);

		return getConfigurationService().getValue(MessageFormat.format("{0}.{1}.wf", PROPERTY_WF_PREFIX, entityName));
	}

}
