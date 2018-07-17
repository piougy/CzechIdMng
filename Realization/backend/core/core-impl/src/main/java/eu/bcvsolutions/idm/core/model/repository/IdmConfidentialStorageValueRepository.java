package eu.bcvsolutions.idm.core.model.repository;

import java.util.UUID;

import javax.transaction.Transactional;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmConfidentialStorageValue;

/**
 * "Naive" confidential storage repository
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmConfidentialStorageValueRepository extends AbstractEntityRepository<IdmConfidentialStorageValue> {
	
	/**
	 * Finds unique storge value
	 * 
	 * @param ownerType
	 * @param ownerId
	 * @param key
	 * @return
	 */
	IdmConfidentialStorageValue findOneByOwnerIdAndOwnerTypeAndKey(UUID ownerId, String ownerType, String key);
	
	/**
	 * Deletes all values by given key from all owners
	 * 
	 * @param key
	 * @return
	 */
	@Transactional
	int deleteByKey(String key);

	/**
	 * Deletes all values by given owner. Use this method after delete whole owner.
	 * 
	 * @param ownerId
	 * @param ownerType
	 * @return
	 * @since 7.6.0
	 */
	@Transactional
	int deleteByOwnerIdAndOwnerType(UUID ownerId, String ownerType);
}
