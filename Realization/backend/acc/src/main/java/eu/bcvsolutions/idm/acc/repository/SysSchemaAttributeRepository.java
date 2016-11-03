package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;

/**
 * Schema attributes
 * 
 * @author Svanda
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "schemaAttributes", //
		path = "schemaAttributes", //
		itemResourceRel = "schemaAttribute", //
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
	)
public interface SysSchemaAttributeRepository extends BaseRepository<SysSchemaAttribute, EmptyFilter> {
	
	@Override
	@Query(value = "select e from SysSchemaAttribute e")
	Page<SysSchemaAttribute> find(EmptyFilter filter, Pageable pageable);
}
