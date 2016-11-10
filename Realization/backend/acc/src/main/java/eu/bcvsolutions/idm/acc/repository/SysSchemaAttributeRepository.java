package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.dto.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.repository.projection.SysSchemaAttributeExcerpt;
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
		excerptProjection = SysSchemaAttributeExcerpt.class, //
		exported = false // we are using repository metadata, but we want expose
							// rest endpoint manually
)
public interface SysSchemaAttributeRepository extends BaseRepository<SysSchemaAttribute, SchemaAttributeFilter> {

	@Override
	@Query(value = "select e from SysSchemaAttribute e" + " where"
			+ " (?#{[0].objectClassId} is null or e.objectClass.id = ?#{[0].objectClassId})"
		    + " and"
	        + " (?#{[0].systemId} is null or e.objectClass.system.id = ?#{[0].systemId})"
			+ " and"
			+ " (lower(e.name) like ?#{[0].name == null ? '%' : '%'.concat([0].name.toLowerCase()).concat('%')})")
	Page<SysSchemaAttribute> find(SchemaAttributeFilter filter, Pageable pageable);
}
