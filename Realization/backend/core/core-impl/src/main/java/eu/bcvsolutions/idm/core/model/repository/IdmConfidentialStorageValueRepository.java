package eu.bcvsolutions.idm.core.model.repository;

import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.data.repository.query.Param;

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
	IdmConfidentialStorageValue findOneByOwnerIdAndOwnerTypeAndKey(
			@Param("ownerId") UUID ownerId,
			@Param("ownerType") String ownerType,			
			@Param("key") String key);
	
	/**
	 * Deletes all values by given key from all owners
	 * 
	 * @param key
	 * @return
	 */
	@Transactional
	int deleteByKey(@Param("key") String key);

	/**
	 * Deletes all values by given owner. Use this method after delete whole owner.
	 * 
	 * @param ownerId
	 * @param ownerType
	 * @return
	 * @since 7.6.0
	 */
	@Transactional
	int deleteByOwnerIdAndOwnerType(
			@Param("ownerId") UUID ownerId,
			@Param("ownerType") String ownerType);
}
