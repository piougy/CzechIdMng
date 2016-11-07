package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.repository.projection.SysSchemaObjectClassExcerpt;
import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;

/**
 * Schema object class
 * 
 * @author Svanda
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "schemaObjectClasses", //
		path = "schemaObjectClasses", //
		itemResourceRel = "schemaObjectClass", //
		excerptProjection = SysSchemaObjectClassExcerpt.class,//
		exported = false // we are using repository metadata, but we want expose rest endpoint manually
	)
public interface SysSchemaObjectClassRepository extends BaseRepository<SysSchemaObjectClass, EmptyFilter> {

	@Override
	@Query(value = "select e from SysSchemaObjectClass e")
	Page<SysSchemaObjectClass> find(EmptyFilter filter, Pageable pageable);
}
