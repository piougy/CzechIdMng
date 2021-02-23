package eu.bcvsolutions.idm.core.security.api.service;

import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;

/**
 * Provide ticket validation for CAS
 *
 * @author Roman Kučera
 */
public interface CasValidationService {

	/**
	 * Validate token in CAS
	 * @param token which will be validated
	 * @param propertyIdmUrl IdM URL - same as in CAS service configuration
	 * @param propertyCasUrl CAS URL - where CAS is accessible
	 * @return
	 */
	Assertion validate(String token, String propertyIdmUrl, String propertyCasUrl) throws TicketValidationException;
}
