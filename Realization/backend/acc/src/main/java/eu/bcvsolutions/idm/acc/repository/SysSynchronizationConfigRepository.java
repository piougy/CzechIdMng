package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.dto.SynchronizationConfigFilter;
import eu.bcvsolutions.idm.acc.dto.SystemEntityHandlingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSynchronizationConfig;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.acc.rest.projection.SysSynchronizationConfigExcerpt;
import eu.bcvsolutions.idm.acc.rest.projection.SysSystemEntityHandlingExcerpt;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Synchronization config repository
 * 
 * @author Svanda
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "synchronizationConfigs", //
		path = "synchronization-configs", //
		itemResourceRel = "synchronizationConfig", //
		excerptProjection = SysSynchronizationConfigExcerpt.class,
		exported = false // we are using repository metadata, but we want expose
							// rest endpoint manually
)
public interface SysSynchronizationConfigRepository extends AbstractEntityRepository<SysSynchronizationConfig, SynchronizationConfigFilter> {

	@Override
	@Query(value = "select e from SysSynchronizationConfig e"+ 
			" where" +
	        " (?#{[0].systemId} is null or e.attributeMapping.system.id = ?#{[0].systemId})"
			)
	Page<SysSynchronizationConfig> find(SynchronizationConfigFilter filter, Pageable pageable);
}
