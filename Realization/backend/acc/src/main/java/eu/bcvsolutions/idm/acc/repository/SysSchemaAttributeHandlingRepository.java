package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.core.api.dto.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.BaseRepository;

/**
 * Schema attributes handling
 * 
 * @author Svanda
 *
 */
@RepositoryRestResource(//
		collectionResourceRel = "schemaAttributesHandling", //
		path = "schemaAttributesHandling", //
		itemResourceRel = "schemaAttributeHandling", //
		exported = false // we are using repository metadata, but we want expose
							// rest endpoint manually
)
public interface SysSchemaAttributeHandlingRepository extends BaseRepository<SysSchemaAttributeHandling, EmptyFilter> {
	@Override
	@Query(value = "select e from SysSchemaAttributeHandling e")
	Page<SysSchemaAttributeHandling> find(EmptyFilter filter, Pageable pageable);
}
