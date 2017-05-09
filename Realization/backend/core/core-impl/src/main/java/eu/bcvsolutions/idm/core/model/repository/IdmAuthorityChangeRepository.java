package eu.bcvsolutions.idm.core.model.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorityChange;

/**
 * Authority change repository.
 * 
 * @author Jan Helbich
 *
 */
public interface IdmAuthorityChangeRepository extends
		AbstractEntityRepository<IdmAuthorityChange, EmptyFilter> {
	
	@Override
	@Query(value = "select e from #{#entityName} e")
	Page<IdmAuthorityChange> find(EmptyFilter filter, Pageable pageable);

	IdmAuthorityChange findOneByIdentity_Id(UUID identityId);
	
}
