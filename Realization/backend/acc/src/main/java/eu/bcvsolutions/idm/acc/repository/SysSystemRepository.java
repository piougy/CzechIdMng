package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.repository.projection.SysSystemExcerpt;
import eu.bcvsolutions.idm.core.model.repository.BaseRepository;

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
public interface SysSystemRepository extends BaseRepository<SysSystem> {

	SysSystem findOneByName(@Param("name") String name);
}
