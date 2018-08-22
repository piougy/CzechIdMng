package eu.bcvsolutions.idm.core.api.service;

import org.joda.time.DateTime;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTokenFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;

/**
 * Persisted tokens. Use {@link TokenManager} instead in your services.
 * 
 * @see TokenManager
 * @author Radek Tomiška
 * @since 8.2.0
 */
public interface IdmTokenService extends 
		EventableDtoService<IdmTokenDto, IdmTokenFilter>,
		AuthorizableService<IdmTokenDto> {
	
	/**
	 * Returns owner type - owner type has to be entity class - dto class can be given.
	 * Its used as default definition type for given owner type.
	 * 
	 * @see LookupService#getOwnerType(Class)
	 * @param ownerType
	 * @return
	 */
	String getOwnerType(Class<? extends Identifiable> ownerType);
	
	/**
	 * Delete tokens with expiration older than given
	 * - can be called from scheduler
	 * 
	 * @see TokenManager
	 * @param tokenType - [optional] - given type only (e.g. cidmst)
	 * @param olderThan - [optional] - with expiration older than given, all otherwise
	 */
	void purgeTokens(String tokenType, DateTime olderThan);
}
