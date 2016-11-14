package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.dto.SystemEntityHandlingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.acc.repository.projection.SysSystemEntityHandlingExcerpt;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

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
public interface SysSystemEntityHandlingRepository extends AbstractEntityRepository<SysSystemEntityHandling, SystemEntityHandlingFilter> {

	@Override
	@Query(value = "select e from SysSystemEntityHandling e"+ 
			" where" +
	        " (?#{[0].systemId} is null or e.system.id = ?#{[0].systemId})"+
			" and" +
			" (?#{[0].operationType} is null or e.operationType = ?#{[0].operationType})"+
			" and" +
			" (?#{[0].entityType} is null or e.entityType = ?#{[0].entityType})"
			)
	Page<SysSystemEntityHandling> find(SystemEntityHandlingFilter filter, Pageable pageable);
}
