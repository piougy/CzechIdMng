package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.entity.SysConnectorServer;
import eu.bcvsolutions.idm.acc.rest.projection.SysConnectorServerExcerpt;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Default system connector server repository
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RepositoryRestResource(//
		collectionResourceRel = "connectorServers", //
		path = "connector-servers", //
		itemResourceRel = "connectorServers", //
		excerptProjection = SysConnectorServerExcerpt.class, //
		exported = false //
	)
public interface SysConnectorServerRepository extends AbstractEntityRepository<SysConnectorServer, QuickFilter> {
	
	SysConnectorServer findOneByName(@Param("name") String name);
	
	@Override
	@Query(value = "SELECT e FROM SysConnectorServer e" +
	        " WHERE" +
	        "(?#{[0].text} IS NULL OR lower(e.name) LIKE ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')}) ")
	Page<SysConnectorServer> find(QuickFilter filter, Pageable pageable);
}
