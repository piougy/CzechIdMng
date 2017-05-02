package eu.bcvsolutions.idm.core.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorityChange;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

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

	IdmAuthorityChange findByIdentity(@Param("identity") IdmIdentity identity);
	
}
