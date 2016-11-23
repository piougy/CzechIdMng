package eu.bcvsolutions.idm.core.model.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmConfidentialStorageValue;

/**
 * "Naive" confidential storage repository
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmConfidentialStorageValueRepository extends AbstractEntityRepository<IdmConfidentialStorageValue, EmptyFilter> {

	@Override
	@Query(value = "select e from #{#entityName} e")
	Page<IdmConfidentialStorageValue> find(EmptyFilter filter, Pageable pageable);
	
	IdmConfidentialStorageValue findOneByOwnerTypeAndOwnerIdAndKey(
			@Param("ownerType") String ownerType,
			@Param("ownerId") UUID ownerId,
			@Param("key") String key);
	
}
