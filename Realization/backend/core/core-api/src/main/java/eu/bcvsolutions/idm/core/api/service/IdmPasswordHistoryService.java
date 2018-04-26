package eu.bcvsolutions.idm.core.api.service;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.IdmPasswordHistoryDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordHistoryFilter;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Service for check password history
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IdmPasswordHistoryService extends ReadWriteDtoService<IdmPasswordHistoryDto, IdmPasswordHistoryFilter> {

	/**
	 * Check password equals with password trough history.
	 * Beware on some situation doesn't exists password history record.
	 *
	 * @param identityId - id of identity, for this identity will be done check in password history
	 * @param countOfIteration - count of back iteration.
	 * @param newPassword - new password
	 * @return true if founded some equals password in history, otherwise return false
	 */
	boolean checkHistory(UUID identityId, int countOfIteration, GuardedString newPassword);
	
	/**
	 * Remove all password history record by identity.
	 *
	 * @param identityId
	 */
	void deleteAllByIdentity(UUID identityId);
}
