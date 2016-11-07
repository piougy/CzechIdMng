package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.acc.repository.projection.SysSchemaObjectClassExcerpt;
import eu.bcvsolutions.idm.acc.repository.projection.SysSystemEntityHandlingExcerpt;
import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;

/**
 * System entity handling
 * 
 * @author Svanda
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "systemEntitiesHandling", //
		path = "systemEntitiesHandling", //
		itemResourceRel = "systemEntityHandling", //
		excerptProjection = SysSystemEntityHandlingExcerpt.class,
		exported = false // we are using repository metadata, but we want expose
							// rest endpoint manually
)
public interface SysSystemEntityHandlingRepository extends BaseRepository<SysSystemEntityHandling, EmptyFilter> {

	@Override
	@Query(value = "select e from SysSystemEntityHandling e")
	Page<SysSystemEntityHandling> find(EmptyFilter filter, Pageable pageable);
}
