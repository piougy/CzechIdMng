package eu.bcvsolutions.idm.acc.security.authentication.impl;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.security.api.authentication.AbstractAuthenticator;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.JwtAuthenticationService;
import eu.bcvsolutions.idm.core.security.api.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassImpl;

/**
 * Abstract class for acc module authenticator that authenticate over system.
 *
 * @author Ondrej Kopr
 *
 */
public abstract class AbstractAccAuthenticator extends AbstractAuthenticator {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractAccAuthenticator.class);

	@Autowired
	protected SysSystemService systemService;
	@Autowired
	protected LookupService lookupService;
	@Autowired
	protected ProvisioningService provisioningService;
	@Autowired
	protected AccAccountService accountService;
	@Autowired
	protected SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired
	protected JwtAuthenticationService jwtAuthenticationService;
	@Autowired
	protected SysSchemaAttributeService schemaAttributeService;
	@Autowired
	protected SysSystemEntityService systemEntityService;

	/**
	 * Get identity for given {@link LoginDto}. Identity will be searched by username.
	 *
	 * @param loginDto
	 * @return
	 * @deprecated @since 10.7.0 - use {@link #getValidIdentity(String, boolean)}
	 */
	@Deprecated
	protected IdmIdentityDto getIdentity(LoginDto loginDto) {
		IdmIdentityDto identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, loginDto.getUsername());
		if (identity == null) {	
			throw new IdmAuthenticationException(MessageFormat.format("Check identity can login: The identity [{0}] either doesn't exist or is deleted.", loginDto.getUsername()));
		}
		return identity;
	}

	/**
	 * Process authentication against given system with login and password.
	 *
	 * @param systemCodeable
	 * @param loginDto
	 * @param identity
	 * @return
	 */
	protected IcUidAttribute authenticateOverSystem(SysSystemDto system, LoginDto loginDto, IdmIdentityDto identity) {
		// search authentication attribute for system with provisioning mapping, only for identity
		SysSystemAttributeMappingDto attribute = systemAttributeMappingService.getAuthenticationAttribute(system.getId(), SystemEntityType.IDENTITY);
		//
		if (attribute == null) {
			// attribute doesn't exists
			LOG.error("System id [{}] is configured for authenticate, but for the system doesn't exist authentication attribute.", system.getId());
			return null;
		}
		//
		// find if identity has account on system
		List<AccAccountDto> accounts = accountService.getAccounts(system.getId(), identity.getId());
		if (accounts.isEmpty()) {
			LOG.debug("Identity id [{}] hasn't account for system id [{}].", identity.getId(), system.getId());
			// user hasn't account on system, continue
			return null;
		}
		//
		IcUidAttribute auth = null;
		//
		// authenticate over all accounts find first, or throw error
		for (AccAccountDto account : accounts) {
			SysSchemaAttributeDto schemaAttribute = schemaAttributeService.get(attribute.getSchemaAttribute());
			SysSchemaObjectClassDto schemaObjectClassDto = DtoUtils.getEmbedded(schemaAttribute, SysSchemaAttribute_.objectClass);
			SysSystemEntityDto systemEntityDto = systemEntityService.get(account.getSystemEntity());
			IcObjectClass objectClass = new IcObjectClassImpl(schemaObjectClassDto.getObjectClassName());

			String transformUsername = null;
			if (!attribute.isUid()) {
				IcConnectorObject connectorObject = systemService.readConnectorObject(system.getId(), systemEntityDto.getUid(), objectClass);
				//
				if (connectorObject == null) {
					continue;
				}
				// iterate over all attributes to find authentication attribute
				for (IcAttribute icAttribute : connectorObject.getAttributes()) {

					if (icAttribute.getName().equals(schemaAttributeService.get(attribute.getSchemaAttribute()).getName())) {
						transformUsername = String.valueOf(icAttribute.getValue());
						break;
					}
				}
				if (transformUsername == null) {
					LOG.error("For system id [{}] cant be transformed username for identity id [{}]. The system will be skipped for autentication.", system.getId(), identity.getId());
					return null;
				}
			} else {
				transformUsername = systemEntityDto.getUid();
			}

			//
			// authentication over system, when password or username not exist or bad credentials - throw error
			try {
				// authentication against system
				auth = provisioningService.authenticate(transformUsername, loginDto.getPassword(), system, SystemEntityType.IDENTITY);
				// check auth
				if (auth == null || auth.getValue() == null) {
					// failed, continue to another account
					continue;
				}
				// everything success break and the authentication will be returned
				break;
			} catch (ResultCodeException e) {
				String message = StringUtils.trimToEmpty(e.getMessage());
				LOG.error("Authentication trought system name [{}] for identity username [{}] failed! Error message: [{}]", system.getCode(), identity.getUsername(), message);
			}
		}
		return auth;
	}
}
