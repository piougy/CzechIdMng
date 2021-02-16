package eu.bcvsolutions.idm.core.security.auth.filter;

import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.config.domain.CasConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.filter.AbstractAuthenticationFilter;
import eu.bcvsolutions.idm.core.security.api.service.JwtAuthenticationService;
import eu.bcvsolutions.idm.core.security.exception.IdmAuthenticationException;

/**
 * Filter which will authenticate user against CAS
 * @author Roman Kučera
 */

@Order(50)
@Component("casIdmAuthenticationFilter")
@Enabled(module = CoreModuleDescriptor.MODULE_ID)
public class CasIdmAuthenticationFilter extends AbstractAuthenticationFilter {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CasIdmAuthenticationFilter.class);

	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private CasConfiguration casConfiguration;
	@Autowired
	private JwtAuthenticationService jwtAuthenticationService;

	@Override
	public boolean authorize(String token, HttpServletRequest req, HttpServletResponse res) {
		String propertyCasUrl = casConfiguration.getPropertyCasUrl();
		String propertyIdmUrl = casConfiguration.getPropertyIdmUrl();

		if (StringUtils.isBlank(propertyIdmUrl) || StringUtils.isBlank(propertyCasUrl)) {
			LOG.info("URL for CAS and IdM is not set in configuration, can not validate ticket");
			return false;
		}

		try {
			if (StringUtils.isBlank(token)) {
				LOG.info("No session from CAS");
				return false;
			}

			Cas30ServiceTicketValidator validator = new Cas30ServiceTicketValidator(propertyCasUrl);
			validator.setRenew(false);
			Assertion assertion = validator.validate(token, propertyIdmUrl);

			if (assertion == null || assertion.getPrincipal() == null) {
				LOG.info("No principal name.");
				return false;
			}

			String userName = assertion.getPrincipal().getName();
			if (userName == null) {
				LOG.info("No username for user.");
				return false;
			}

			LOG.info("Username found [{}]", userName);

			IdmIdentityDto identity = identityService.getByUsername(userName);

			if (identity == null) {
				throw new IdmAuthenticationException(MessageFormat.format(
						"Check identity can login: The identity "
								+ "[{0}] either doesn't exist or is deleted.",
						userName));
			}

			if (assertion.isValid()) {
				LoginDto fullLoginDto = jwtAuthenticationService.createJwtAuthenticationAndAuthenticate(createLoginDto(userName),
						identity, CoreModuleDescriptor.MODULE_ID);
				return fullLoginDto != null;
			}

			return false;
		} catch (IdmAuthenticationException e) {
			LOG.warn("Authentication exception raised during CAS authentication: [{}].", e.getMessage(), e);
		} catch (Exception e) {
			LOG.error("Exception was raised during CAS authentication: [{}].", e.getMessage(), e);
		}

		return false;
	}

	private LoginDto createLoginDto(String userName) {
		LoginDto ldto = new LoginDto();
		ldto.setUsername(userName);
		return ldto;
	}

	@Override
	public String getAuthorizationHeaderName() {
		return casConfiguration.getPropertyHeaderName();
	}

	@Override
	public String getAuthorizationHeaderPrefix() {
		return casConfiguration.getPropertyHeaderPrefix();
	}

	@Override
	public boolean isDisabled() {
		return !casConfiguration.getPropertyCasSsoEnabled();
	}
}