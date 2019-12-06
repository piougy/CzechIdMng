package eu.bcvsolutions.idm.core.model.repository;

import java.time.ZonedDateTime;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmToken;

/**
 * Token repository.
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmTokenRepository extends AbstractEntityRepository<IdmToken> {
	
	/**
	 * Delete tokens with expiration older than given
	 * 
	 * @param olderThan
	 * @return
	 */
	long deleteByExpirationLessThan(ZonedDateTime olderThan);
	
	/**
	 * Delete all tokens with token type
	 * 
	 * @param tokenType
	 * @return
	 */
	long deleteByTokenType(String tokenType);
	
	/**
	 * Delete tokens with token type and expiration older than given
	 * 
	 * @param tokenType
	 * @param olderThan
	 * @return
	 */
	long deleteByTokenTypeAndExpirationLessThan(String tokenType, ZonedDateTime olderThan);
}
