package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.dto.SchemaAttributeHandlingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.acc.repository.projection.SysSchemaAttributeHandlingExcerpt;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;

/**
 * Schema attributes handling repository
 * 
 * @author Svanda
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "schemaAttributesHandling", //
		path = "schemaAttributesHandling", //
		itemResourceRel = "schemaAttributeHandling", //
		excerptProjection = SysSchemaAttributeHandlingExcerpt.class,//
		exported = false // we are using repository metadata, but we want expose
							// rest endpoint manually
)
public interface SysSchemaAttributeHandlingRepository extends BaseRepository<SysSchemaAttributeHandling, SchemaAttributeHandlingFilter> {
	@Override
	@Query(value = "select e from SysSchemaAttributeHandling e" +
			" where" +
	        " (?#{[0].entityHandlingId} is null or e.systemEntityHandling.id = ?#{[0].entityHandlingId})"
	        + " and"
	        + " (?#{[0].systemId} is null or e.systemEntityHandling.system.id = ?#{[0].systemId})"
	        + " and"
			+ " (?#{[0].idmPropertyName} is null or lower(e.idmPropertyName) like ?#{[0].idmPropertyName == null ? '%' : '%'.concat([0].idmPropertyName.toLowerCase()).concat('%')})"
			)
	Page<SysSchemaAttributeHandling> find(SchemaAttributeHandlingFilter filter, Pageable pageable);
}
