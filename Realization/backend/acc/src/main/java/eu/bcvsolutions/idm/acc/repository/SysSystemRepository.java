package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.rest.projection.SysSystemExcerpt;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Target system configuration
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "systems", //
		path = "systems", //
		itemResourceRel = "system", //
		excerptProjection = SysSystemExcerpt.class,
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
	)
public interface SysSystemRepository extends AbstractEntityRepository<SysSystem, QuickFilter> {

	SysSystem findOneByName(@Param("name") String name);
	
	@Override
	@Query(value = "select e from SysSystem e" +
	        " where" +
	        " lower(e.name) like :#{#filter.text == null ? '%' : '%'.concat(#filter.text.toLowerCase()).concat('%')}")
	Page<SysSystem> find(@Param(value = "filter") QuickFilter filter, Pageable pageable);
	
}
