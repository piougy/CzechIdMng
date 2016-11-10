package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.dto.SchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.repository.projection.SysSchemaObjectClassExcerpt;
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
public interface SysSchemaObjectClassRepository extends BaseRepository<SysSchemaObjectClass, SchemaObjectClassFilter> {

	@Override
	@Query(value = "select e from SysSchemaObjectClass e" +
	        " where" +
	        " (?#{[0].systemId} is null or e.system.id = ?#{[0].systemId})" +
	        " and" +
	        " (lower(e.objectClassName) like ?#{[0].objectClassName == null ? '%' : '%'.concat([0].objectClassName.toLowerCase()).concat('%')})")
	Page<SysSchemaObjectClass> find(SchemaObjectClassFilter filter, Pageable pageable);
}
